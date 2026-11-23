package entities.template_creation;

import entities.Exercise;

import java.util.List;
import java.util.Objects;

/**
 * Immutable planned sets for one exercise occurrence in a created template.
 * setId identifies the group within its template; set positions are zero-based.
 * An empty measurement list means that measurement is unused for this group.
 * All nonempty lists must describe the same number of sets.
 * Units, numeric ranges, and minimum set counts are left to the agreed save rules.
 */
public final class ExerciseSetGroup {
    private final Exercise exercise;
    private final int setId;
    private final List<Double> weights;
    private final List<Integer> reps;
    private final List<Integer> time;
    private final List<Integer> distance;
    private final int setCount;

    public ExerciseSetGroup(Exercise exercise, int setId, List<Double> weights,
                            List<Integer> reps, List<Integer> time, List<Integer> distance) {
        this.exercise = Objects.requireNonNull(exercise, "exercise");
        this.setId = setId;
        this.weights = List.copyOf(weights);
        this.reps = List.copyOf(reps);
        this.time = List.copyOf(time);
        this.distance = List.copyOf(distance);
        int count = 0;
        for (List<?> measurements : List.of(this.weights, this.reps, this.time, this.distance)) {
            if (!measurements.isEmpty()) {
                if (count != 0 && measurements.size() != count) {
                    throw new IllegalArgumentException("Nonempty measurement lists must have the same set count");
                }
                count = measurements.size();
            }
        }
        this.setCount = count;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public int getSetId() {
        return setId;
    }

    public int getSetCount() {
        return setCount;
    }

    public double getWeight(int setIndex) {
        return weights.get(setIndex);
    }

    public int getReps(int setIndex) {
        return reps.get(setIndex);
    }

    public int getTime(int setIndex) {
        return time.get(setIndex);
    }

    public int getDistance(int setIndex) {
        return distance.get(setIndex);
    }

    public List<Double> getWeights() {
        return weights;
    }

    public List<Integer> getReps() {
        return reps;
    }

    public List<Integer> getTime() {
        return time;
    }

    public List<Integer> getDistance() {
        return distance;
    }
}
