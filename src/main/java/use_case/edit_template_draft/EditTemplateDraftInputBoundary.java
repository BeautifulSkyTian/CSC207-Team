package use_case.edit_template_draft;

import entities.Exercise;
import entities.MeasurementType;

import java.util.UUID;

/** Editor actions on the current draft snapshot. Set indexes are zero-based. */
public interface EditTemplateDraftInputBoundary {
    void rename(WorkoutTemplateDraft draft, String name);

    void addExercise(WorkoutTemplateDraft draft, Exercise exercise, MeasurementType measurementType);

    void changeMeasurementType(WorkoutTemplateDraft draft, UUID rowId, MeasurementType measurementType);

    void removeExercise(WorkoutTemplateDraft draft, UUID rowId);

    void addSet(WorkoutTemplateDraft draft, UUID rowId);

    void updateSet(WorkoutTemplateDraft draft, UUID rowId, int setIndex, DraftSet values);

    void removeSet(WorkoutTemplateDraft draft, UUID rowId, int setIndex);
}
