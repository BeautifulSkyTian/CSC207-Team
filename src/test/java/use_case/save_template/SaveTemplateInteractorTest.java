package use_case.save_template;

import data_access.JsonTemplateDataAccessObject;
import entities.Exercise;
import entities.MeasurementType;
import entities.Template;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.edit_template_draft.DraftExerciseGroup;
import use_case.edit_template_draft.DraftSet;
import use_case.edit_template_draft.WorkoutTemplateDraft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SaveTemplateInteractorTest {
    @TempDir
    Path tempDirectory;

    private final Exercise exercise = new Exercise.ExerciseBuilder().id("exercise-1").name("Exercise").build();
    private final RecordingPresenter presenter = new RecordingPresenter();

    @Test
    void bothMeasurementTypesRoundTripAndSuccessReturnsThePersistedIdentity() {
        Path file = tempDirectory.resolve("templates.json");
        JsonTemplateDataAccessObject dao = new JsonTemplateDataAccessObject(file.toString());
        SaveTemplateInputBoundary interactor = new SaveTemplateInteractor(dao, presenter);
        WorkoutTemplateDraft draft = new WorkoutTemplateDraft("  Mixed workout  ", List.of(
                new DraftExerciseGroup(UUID.randomUUID(), exercise, List.of(new DraftSet("40.5", " 10 "))),
                new DraftExerciseGroup(UUID.randomUUID(), exercise, MeasurementType.TIME_DISTANCE,
                        List.of(new DraftSet("", "", "60", "0.25")))));

        interactor.execute(new SaveTemplateInputData(draft));

        assertNull(presenter.failure);
        assertEquals("Mixed workout", presenter.result.getTemplateName());
        Template saved = new JsonTemplateDataAccessObject(file.toString()).get("Mixed workout");
        assertEquals(presenter.result.getTemplateId(), saved.getTemplateId());
        assertEquals(List.of(40.5), saved.getGroup(0).getWeights());
        assertEquals(List.of(10), saved.getGroup(0).getRepsList());
        assertEquals(List.of(60), saved.getGroup(1).getTimes());
        assertEquals(List.of(0.25), saved.getGroup(1).getDistances());
    }

    @Test
    void invalidDraftsReportFailureWithoutCreatingAFile() {
        Path file = tempDirectory.resolve("templates.json");
        JsonTemplateDataAccessObject dao = new JsonTemplateDataAccessObject(file.toString());
        SaveTemplateInputBoundary interactor = new SaveTemplateInteractor(dao, presenter);

        interactor.execute(new SaveTemplateInputData(new WorkoutTemplateDraft(" ", List.of())));
        assertTrue(presenter.failure.contains("name"));
        interactor.execute(new SaveTemplateInputData(new WorkoutTemplateDraft("Plan", List.of())));
        assertTrue(presenter.failure.contains("exercise"));
        interactor.execute(new SaveTemplateInputData(draftWithSet("Plan", new DraftSet("30", "8.5"))));
        assertTrue(presenter.failure.contains("set 1: reps"));
        interactor.execute(new SaveTemplateInputData(draftWithSet("Plan", new DraftSet("Infinity", "8"))));
        assertTrue(presenter.failure.contains("set 1: weight"));
        interactor.execute(new SaveTemplateInputData(new WorkoutTemplateDraft("Intervals", List.of(
                new DraftExerciseGroup(UUID.randomUUID(), exercise, MeasurementType.TIME_DISTANCE,
                        List.of(new DraftSet("", "", "", "0.25")))))));
        assertTrue(presenter.failure.contains("set 1: time"));

        assertNull(presenter.result);
        assertTrue(dao.getAll().isEmpty());
        assertFalse(Files.exists(file));
    }

    @Test
    void duplicateNameIsRejectedAndRenamingAnExistingTemplatePreservesItsId() {
        Path file = tempDirectory.resolve("templates.json");
        JsonTemplateDataAccessObject dao = new JsonTemplateDataAccessObject(file.toString());
        SaveTemplateInputBoundary interactor = new SaveTemplateInteractor(dao, presenter);
        WorkoutTemplateDraft draft = draftWithSet("Before", new DraftSet("20", "8"));
        interactor.execute(new SaveTemplateInputData(draft));
        Template original = dao.get("Before");

        interactor.execute(new SaveTemplateInputData(draft));
        assertNull(presenter.result);
        assertTrue(presenter.failure.contains("already uses that name"));
        assertEquals(original.getTemplateId(), dao.get("Before").getTemplateId());

        WorkoutTemplateDraft existing = WorkoutTemplateDraft.fromTemplate(original);
        interactor.execute(new SaveTemplateInputData(existing));
        assertNull(presenter.failure);
        assertEquals(original.getTemplateId(), presenter.result.getTemplateId());

        WorkoutTemplateDraft renamed = new WorkoutTemplateDraft(existing.getTemplateId(), "After", existing.getGroups());
        interactor.execute(new SaveTemplateInputData(renamed));
        JsonTemplateDataAccessObject reloaded = new JsonTemplateDataAccessObject(file.toString());
        assertNull(presenter.failure);
        assertNull(reloaded.get("Before"));
        assertEquals(original.getTemplateId(), reloaded.get("After").getTemplateId());
        assertEquals(1, reloaded.getAll().size());
    }

    @Test
    void failedWriteReportsFailureAndTheSameDraftCanBeRetried() throws IOException {
        Path file = tempDirectory.resolve("templates.json");
        JsonTemplateDataAccessObject dao = new JsonTemplateDataAccessObject(file.toString());
        SaveTemplateInputBoundary interactor = new SaveTemplateInteractor(dao, presenter);
        SaveTemplateInputData input = new SaveTemplateInputData(draftWithSet("Retry me", new DraftSet("20", "8")));
        Files.createDirectory(file);
        Files.createFile(file.resolve("keep-directory-nonempty"));

        interactor.execute(input);
        assertNull(presenter.result);
        assertTrue(presenter.failure.contains("Could not write"));
        assertTrue(dao.getAll().isEmpty());

        Files.delete(file.resolve("keep-directory-nonempty"));
        Files.delete(file);
        interactor.execute(input);
        assertNull(presenter.failure);
        assertEquals(presenter.result.getTemplateId(),
                new JsonTemplateDataAccessObject(file.toString()).get("Retry me").getTemplateId());
    }

    private WorkoutTemplateDraft draftWithSet(String name, DraftSet set) {
        return new WorkoutTemplateDraft(name, List.of(new DraftExerciseGroup(UUID.randomUUID(), exercise, List.of(set))));
    }

    private static class RecordingPresenter implements SaveTemplateOutputBoundary {
        private SaveTemplateOutputData result;
        private String failure;

        @Override
        public void presentSuccess(SaveTemplateOutputData result) {
            this.result = result;
            failure = null;
        }

        @Override
        public void presentFailure(String message) {
            result = null;
            failure = message;
        }
    }
}
