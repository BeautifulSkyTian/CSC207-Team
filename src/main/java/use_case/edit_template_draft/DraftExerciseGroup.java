package use_case.edit_template_draft;

import entities.Exercise;
import entities.ExerciseSetGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable editor snapshot of one exercise occurrence and its weight/rep sets.
 * rowId identifies the occurrence only while editing; it is not a saved group ID.
 */
public final class DraftExerciseGroup {
    private final UUID rowId;
    private final Exercise exercise;
    private final List<DraftSet> sets;

    public DraftExerciseGroup(UUID rowId, Exercise exercise, List<DraftSet> sets) {
        this.rowId = Objects.requireNonNull(rowId, "rowId");
        this.exercise = Objects.requireNonNull(exercise, "exercise");
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
            draftSets.add(new DraftSet(Double.toString(group.getWeight(index)),
                    Integer.toString(group.getReps(index))));
        }
        return new DraftExerciseGroup(UUID.randomUUID(), group.getExercise(), draftSets);
    }

    public UUID getRowId() {
        return rowId;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public List<DraftSet> getSets() {
        return sets;
    }

    public int getSetCount() {
        return sets.size();
    }
}
