package use_case.edit_template_draft;

import entities.Exercise;
import entities.ExerciseSetGroup;
import entities.MeasurementType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable editor snapshot of one exercise occurrence and its typed sets.
 * rowId identifies the occurrence only while editing; it is not a saved group ID.
 */
public final class DraftExerciseGroup {
    private final UUID rowId;
    private final Exercise exercise;
    private final MeasurementType measurementType;
    private final List<DraftSet> sets;

    public DraftExerciseGroup(UUID rowId, Exercise exercise, List<DraftSet> sets) {
        this(rowId, exercise, MeasurementType.WEIGHT_REPS, sets);
    }

    public DraftExerciseGroup(UUID rowId, Exercise exercise, MeasurementType measurementType, List<DraftSet> sets) {
        this.rowId = Objects.requireNonNull(rowId, "rowId");
        this.exercise = Objects.requireNonNull(exercise, "exercise");
        this.measurementType = Objects.requireNonNull(measurementType, "measurementType");
        this.sets = List.copyOf(sets);
    }

    /**
     * Snapshots a shared group for a new editing session. The mutable group is
     * not retained; only its immutable Exercise reference and set values are used.
     */
    public static DraftExerciseGroup fromGroup(ExerciseSetGroup group) {
        Objects.requireNonNull(group, "group");
        List<DraftSet> draftSets = new ArrayList<>();
        for (int index = 0; index < group.getSetCount(); index++) {
            if (group.getMeasurementType() == MeasurementType.WEIGHT_REPS) {
                draftSets.add(new DraftSet(Double.toString(group.getWeight(index)),
                        Integer.toString(group.getReps(index))));
            } else {
                draftSets.add(new DraftSet("", "", Integer.toString(group.getTime(index)),
                        Double.toString(group.getDistance(index))));
            }
        }
        return new DraftExerciseGroup(UUID.randomUUID(), group.getExercise(), group.getMeasurementType(), draftSets);
    }

    public UUID getRowId() {
        return rowId;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public MeasurementType getMeasurementType() {
        return measurementType;
    }

    public List<DraftSet> getSets() {
        return sets;
    }

    public int getSetCount() {
        return sets.size();
    }
}
