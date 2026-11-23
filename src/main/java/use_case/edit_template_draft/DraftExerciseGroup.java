package use_case.edit_template_draft;

import entities.Exercise;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Immutable snapshot of one exercise occurrence, including incomplete sets. */
public final class DraftExerciseGroup {
    private final UUID rowId;
    private final Exercise exercise;
    private final List<DraftSet> sets;

    public DraftExerciseGroup(UUID rowId, Exercise exercise, List<DraftSet> sets) {
        this.rowId = Objects.requireNonNull(rowId, "rowId");
        this.exercise = Objects.requireNonNull(exercise, "exercise");
        this.sets = List.copyOf(sets);
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
