package interface_adapter.edit_template_draft;

import entities.Exercise;
import entities.MeasurementType;
import interface_adapter.create_template.TemplateEditorViewModel;
import use_case.edit_template_draft.DraftSet;
import use_case.edit_template_draft.EditTemplateDraftInputBoundary;
import use_case.edit_template_draft.WorkoutTemplateDraft;

import java.util.UUID;

/** Applies editor actions to the latest draft. Set indexes are zero-based. */
public final class EditTemplateDraftController {
    private final EditTemplateDraftInputBoundary interactor;
    private final TemplateEditorViewModel viewModel;

    public EditTemplateDraftController(EditTemplateDraftInputBoundary interactor,
                                       TemplateEditorViewModel viewModel) {
        this.interactor = interactor;
        this.viewModel = viewModel;
    }

    public void rename(String name) {
        interactor.rename(currentDraft(), name);
    }

    public void addExercise(Exercise exercise, MeasurementType measurementType) {
        interactor.addExercise(currentDraft(), exercise, measurementType);
    }

    public void changeMeasurementType(UUID rowId, MeasurementType measurementType) {
        interactor.changeMeasurementType(currentDraft(), rowId, measurementType);
    }

    public void removeExercise(UUID rowId) {
        interactor.removeExercise(currentDraft(), rowId);
    }

    public void addSet(UUID rowId) {
        interactor.addSet(currentDraft(), rowId);
    }

    public void updateWeightRepSet(UUID rowId, int setIndex, String weight, String reps) {
        updateSet(rowId, setIndex, new DraftSet(weight, reps));
    }

    public void updateTimeDistanceSet(UUID rowId, int setIndex, String time, String distance) {
        updateSet(rowId, setIndex, new DraftSet("", "", time, distance));
    }

    public void updateSet(UUID rowId, int setIndex, DraftSet values) {
        interactor.updateSet(currentDraft(), rowId, setIndex, values);
    }

    public void removeSet(UUID rowId, int setIndex) {
        interactor.removeSet(currentDraft(), rowId, setIndex);
    }

    private WorkoutTemplateDraft currentDraft() {
        return viewModel.getState().getDraft();
    }
}
