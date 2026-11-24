package interface_adapter.create_template;

import data_access.JsonTemplateDataAccessObject;
import entities.Exercise;
import entities.MeasurementType;
import entities.Template;
import interface_adapter.edit_template_draft.EditTemplateDraftController;
import interface_adapter.edit_template_draft.EditTemplateDraftPresenter;
import interface_adapter.save_template.SaveTemplateController;
import interface_adapter.save_template.SaveTemplatePresenter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.edit_template_draft.EditTemplateDraftInteractor;
import use_case.edit_template_draft.WorkoutTemplateDraft;
import use_case.save_template.SaveTemplateInteractor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateEditorFlowTest {
    @Test
    void saveFeedbackPreservesTheDraftAndFurtherSavesUseTheSameTemplate(@TempDir Path directory) {
        Path templateFile = directory.resolve("templates.json");
        JsonTemplateDataAccessObject templates =
                new JsonTemplateDataAccessObject(templateFile.toString());
        TemplateEditorViewModel viewModel = new TemplateEditorViewModel();
        EditTemplateDraftController edit = new EditTemplateDraftController(
                new EditTemplateDraftInteractor(new EditTemplateDraftPresenter(viewModel)),
                viewModel);
        SaveTemplateController save = new SaveTemplateController(
                new SaveTemplateInteractor(templates, new SaveTemplatePresenter(viewModel)),
                viewModel);
        List<Object> publishedStates = new ArrayList<>();
        viewModel.addPropertyChangeListener(event -> publishedStates.add(event.getNewValue()));
        Exercise exercise = new Exercise.ExerciseBuilder()
                .id("exercise-1").name("Exercise").build();

        edit.rename("  Mixed workout  ");
        edit.addExercise(exercise, MeasurementType.WEIGHT_REPS);
        edit.addExercise(exercise, MeasurementType.TIME_DISTANCE);
        UUID weightRow = viewModel.getState().getDraft().getGroups().get(0).getRowId();
        UUID timedRow = viewModel.getState().getDraft().getGroups().get(1).getRowId();
        edit.addSet(weightRow);
        edit.addSet(timedRow);
        edit.updateWeightRepSet(weightRow, 0, "40.5", "unfinished");
        edit.updateTimeDistanceSet(timedRow, 0, "60", "0.25");
        WorkoutTemplateDraft invalidDraft = viewModel.getState().getDraft();

        save.execute();

        assertSame(invalidDraft, viewModel.getState().getDraft());
        assertTrue(viewModel.getState().getError().contains("reps"));
        assertSame(viewModel.getState(), publishedStates.get(publishedStates.size() - 1));
        assertTrue(templates.getAll().isEmpty());

        edit.updateWeightRepSet(weightRow, 0, "40.5", "10");
        assertNull(viewModel.getState().getError());
        save.execute();

        WorkoutTemplateDraft savedDraft = viewModel.getState().getDraft();
        assertNotNull(savedDraft.getTemplateId());
        assertEquals("Mixed workout", savedDraft.getTemplateName());
        assertEquals(timedRow, savedDraft.getGroups().get(1).getRowId());
        assertEquals("Template saved.", viewModel.getState().getMessage());
        assertSame(viewModel.getState(), publishedStates.get(publishedStates.size() - 1));

        edit.rename("Updated workout");
        assertNull(viewModel.getState().getMessage());
        save.execute();

        JsonTemplateDataAccessObject reloaded =
                new JsonTemplateDataAccessObject(templateFile.toString());
        assertEquals(1, reloaded.getAll().size());
        Template saved = reloaded.get("Updated workout");
        assertNotNull(saved);
        assertEquals(savedDraft.getTemplateId(), saved.getTemplateId());
        assertEquals(List.of(10), saved.getGroup(0).getRepsList());
        assertEquals(List.of(60), saved.getGroup(1).getTimes());

        viewModel.startNewDraft();
        assertNull(viewModel.getState().getDraft().getTemplateId());
        assertTrue(viewModel.getState().getDraft().getGroups().isEmpty());
        assertNull(viewModel.getState().getMessage());
    }

    @Test
    void aStaleSetIndexReportsAnEditErrorWithoutReplacingTheDraft() {
        TemplateEditorViewModel viewModel = new TemplateEditorViewModel();
        EditTemplateDraftController edit = new EditTemplateDraftController(
                new EditTemplateDraftInteractor(new EditTemplateDraftPresenter(viewModel)),
                viewModel);
        Exercise exercise = new Exercise.ExerciseBuilder()
                .id("exercise-1").name("Exercise").build();
        edit.addExercise(exercise, MeasurementType.WEIGHT_REPS);
        WorkoutTemplateDraft draft = viewModel.getState().getDraft();
        UUID rowId = draft.getGroups().get(0).getRowId();

        edit.removeSet(rowId, 0);

        assertSame(draft, viewModel.getState().getDraft());
        assertEquals("This set is no longer in the draft.", viewModel.getState().getError());

        edit.removeExercise(rowId);
        assertTrue(viewModel.getState().getDraft().getGroups().isEmpty());
        assertNull(viewModel.getState().getError());
    }
}
