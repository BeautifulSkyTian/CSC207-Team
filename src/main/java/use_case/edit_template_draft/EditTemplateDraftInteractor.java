package use_case.edit_template_draft;

import entities.Exercise;
import entities.MeasurementType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/** Applies editor actions by building a new snapshot of the draft. */
public final class EditTemplateDraftInteractor implements EditTemplateDraftInputBoundary {
    private final EditTemplateDraftOutputBoundary presenter;

    public EditTemplateDraftInteractor(EditTemplateDraftOutputBoundary presenter) {
        this.presenter = Objects.requireNonNull(presenter);
    }

    @Override
    public void rename(WorkoutTemplateDraft draft, String name) {
        presenter.presentDraft(new WorkoutTemplateDraft(draft.getTemplateId(), name, draft.getGroups()));
    }

    @Override
    public void addExercise(WorkoutTemplateDraft draft, Exercise exercise, MeasurementType measurementType) {
        List<DraftExerciseGroup> groups = new ArrayList<>(draft.getGroups());
        groups.add(new DraftExerciseGroup(UUID.randomUUID(), exercise, measurementType, List.of()));
        presentGroups(draft, groups);
    }

    @Override
    public void removeExercise(WorkoutTemplateDraft draft, UUID rowId) {
        int rowIndex = findRowIndex(draft, rowId);
        if (rowIndex < 0) {
            presenter.presentFailure("This exercise is no longer in the draft.");
            return;
        }

        List<DraftExerciseGroup> groups = new ArrayList<>(draft.getGroups());
        groups.remove(rowIndex);
        presentGroups(draft, groups);
    }

    @Override
    public void addSet(WorkoutTemplateDraft draft, UUID rowId) {
        editSets(draft, rowId, sets -> sets.add(new DraftSet("", "")));
    }

    @Override
    public void updateSet(WorkoutTemplateDraft draft, UUID rowId, int setIndex, DraftSet values) {
        Objects.requireNonNull(values);
        editSets(draft, rowId, sets -> sets.set(setIndex, values));
    }

    @Override
    public void removeSet(WorkoutTemplateDraft draft, UUID rowId, int setIndex) {
        editSets(draft, rowId, sets -> sets.remove(setIndex));
    }

    private void editSets(WorkoutTemplateDraft draft, UUID rowId, Consumer<List<DraftSet>> edit) {
        int rowIndex = findRowIndex(draft, rowId);
        if (rowIndex < 0) {
            presenter.presentFailure("This exercise is no longer in the draft.");
            return;
        }

        List<DraftExerciseGroup> groups = new ArrayList<>(draft.getGroups());
        DraftExerciseGroup row = groups.get(rowIndex);
        List<DraftSet> sets = new ArrayList<>(row.getSets());
        try {
            edit.accept(sets);
        } catch (IndexOutOfBoundsException e) {
            presenter.presentFailure("This set is no longer in the draft.");
            return;
        }

        groups.set(rowIndex, new DraftExerciseGroup(rowId, row.getExercise(), row.getMeasurementType(), sets));
        presentGroups(draft, groups);
    }

    private static int findRowIndex(WorkoutTemplateDraft draft, UUID rowId) {
        List<DraftExerciseGroup> groups = draft.getGroups();
        for (int index = 0; index < groups.size(); index++) {
            if (groups.get(index).getRowId().equals(rowId)) {
                return index;
            }
        }
        return -1;
    }

    private void presentGroups(WorkoutTemplateDraft draft, List<DraftExerciseGroup> groups) {
        presenter.presentDraft(new WorkoutTemplateDraft(draft.getTemplateId(), draft.getTemplateName(), groups));
    }
}
