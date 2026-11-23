package use_case.edit_template_draft;

import entities.Exercise;
import entities.MeasurementType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EditTemplateDraftInteractorTest {
    private final Exercise exercise = new Exercise.ExerciseBuilder().id("run").name("Run").build();
    private final RecordingPresenter presenter = new RecordingPresenter();
    private final EditTemplateDraftInputBoundary interactor = new EditTemplateDraftInteractor(presenter);

    @Test
    void editsKeepTheOriginalSnapshotAndTargetOnlyTheSelectedExerciseOccurrence() {
        WorkoutTemplateDraft original = new WorkoutTemplateDraft("existing-id", "Original", List.of());
        interactor.rename(original, "");
        assertEquals("", presenter.draft.getTemplateName());
        interactor.addExercise(presenter.draft, exercise, MeasurementType.WEIGHT_REPS);
        interactor.addExercise(presenter.draft, exercise, MeasurementType.TIME_DISTANCE);
        WorkoutTemplateDraft beforeSets = presenter.draft;
        DraftExerciseGroup firstRow = beforeSets.getGroups().get(0);
        UUID secondRowId = beforeSets.getGroups().get(1).getRowId();
        assertNotEquals(firstRow.getRowId(), secondRowId);

        interactor.addSet(beforeSets, secondRowId);
        DraftSet blankSet = presenter.draft.getGroups().get(1).getSets().get(0);
        assertEquals("", blankSet.getTimeText());
        assertEquals("", blankSet.getDistanceText());
        interactor.updateSet(presenter.draft, secondRowId, 0, new DraftSet("", "", "12:", "0."));
        WorkoutTemplateDraft edited = presenter.draft;

        assertEquals("existing-id", edited.getTemplateId());
        assertEquals("Original", original.getTemplateName());
        assertTrue(original.getGroups().isEmpty());
        assertTrue(beforeSets.getGroups().get(1).getSets().isEmpty());
        assertSame(firstRow, edited.getGroups().get(0));
        assertEquals(secondRowId, edited.getGroups().get(1).getRowId());
        assertEquals(MeasurementType.TIME_DISTANCE, edited.getGroups().get(1).getMeasurementType());
        assertEquals("12:", edited.getGroups().get(1).getSets().get(0).getTimeText());

        interactor.removeSet(edited, secondRowId, 0);
        assertTrue(presenter.draft.getGroups().get(1).getSets().isEmpty());
        interactor.removeExercise(presenter.draft, firstRow.getRowId());
        assertEquals(1, presenter.draft.getGroups().size());
        assertEquals(secondRowId, presenter.draft.getGroups().get(0).getRowId());
        assertNull(presenter.failure);
    }

    @Test
    void missingRowsAndSetsReportFailureThroughTheOutputBoundary() {
        UUID rowId = UUID.randomUUID();
        WorkoutTemplateDraft draft = new WorkoutTemplateDraft("Plan", List.of(
                new DraftExerciseGroup(rowId, exercise, List.of())));

        interactor.removeExercise(draft, UUID.randomUUID());
        assertTrue(presenter.failure.contains("exercise"));
        interactor.updateSet(draft, rowId, 0, new DraftSet("20", "4"));
        assertTrue(presenter.failure.contains("set"));
        assertNull(presenter.draft);
    }

    private static class RecordingPresenter implements EditTemplateDraftOutputBoundary {
        private WorkoutTemplateDraft draft;
        private String failure;

        @Override
        public void presentDraft(WorkoutTemplateDraft updatedDraft) {
            draft = updatedDraft;
        }

        @Override
        public void presentFailure(String message) {
            failure = message;
        }
    }
}
