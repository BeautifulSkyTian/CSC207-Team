package entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one exercise group within an ongoing workout.
 *
 * Stores a copy of the prescribed exercise group and tracks
 * the completion status of each set.
 */
public class SessionExerciseGroup {

    private final ExerciseSetGroup prescribedGroup;
    private final List<Boolean> completedSets;

    /**
     * Creates a session exercise group from a template group.
     * All sets are initially incomplete.
     *
     * @param group the exercise group from the template
     */
    public SessionExerciseGroup(ExerciseSetGroup group) {
        if (group == null) {
            throw new IllegalArgumentException(
                    "Exercise group cannot be null");
        }

        // Deep copy to avoid modifying the original template.
        this.prescribedGroup = new ExerciseSetGroup(group);

        this.completedSets = new ArrayList<>();

        for (int i = 0; i < group.getSetCount(); i++) {
            completedSets.add(false);
        }
    }

    /**
     * Copy constructor.
     * Preserves completion status while keeping independent data.
     */
    public SessionExerciseGroup(SessionExerciseGroup other) {
        if (other == null) {
            throw new IllegalArgumentException(
                    "Session group cannot be null");
        }

        this.prescribedGroup =
                new ExerciseSetGroup(other.prescribedGroup);

        this.completedSets =
                new ArrayList<>(other.completedSets);
    }

    /**
     * Returns a copy of the prescribed exercise group.
     */
    public ExerciseSetGroup getPrescribedGroup() {
        return new ExerciseSetGroup(prescribedGroup);
    }

    public int getSetCount() {
        return prescribedGroup.getSetCount();
    }

    /**
     * Marks a set as complete or incomplete.
     *
     * @param setIndex zero-based index of the set
     * @param completed new completion status
     */
    public void setCompleted(int setIndex, boolean completed) {
        completedSets.set(setIndex, completed);
    }

    /**
     * Returns whether the specified set is completed.
     */
    public boolean isCompleted(int setIndex) {
        return completedSets.get(setIndex);
    }

    /**
     * Returns the number of completed sets in this group.
     */
    public int getCompletedSetCount() {
        int count = 0;

        for (boolean completed : completedSets) {
            if (completed) {
                count++;
            }
        }

        return count;
    }

    /**
     * Creates a log record containing only completed sets.
     *
     * Returns null if no sets in this group were completed.
     */
    public LogExerciseSet getCompletedLogExercise() {
        List<Double> weights = new ArrayList<>();
        List<Integer> reps = new ArrayList<>();
        List<Integer> times = new ArrayList<>();
        List<Double> distances = new ArrayList<>();

        for (int i = 0; i < getSetCount(); i++) {

            if (isCompleted(i)) {

                if (prescribedGroup.getMeasurementType()
                        == MeasurementType.WEIGHT_REPS) {

                    weights.add(prescribedGroup.getWeight(i));
                    reps.add(prescribedGroup.getReps(i));

                } else if (prescribedGroup.getMeasurementType()
                        == MeasurementType.TIME_DISTANCE) {

                    times.add(prescribedGroup.getTime(i));
                    distances.add(prescribedGroup.getDistance(i));
                }
            }
        }

        if (getCompletedSetCount() == 0) {
            return null;
        }

        Exercise exercise = prescribedGroup.getExercise();

        return new LogExerciseSet(
                exercise.getId(),
                exercise.getName(),
                prescribedGroup.getMeasurementType(),
                weights,
                reps,
                times,
                distances
        );
    }
}