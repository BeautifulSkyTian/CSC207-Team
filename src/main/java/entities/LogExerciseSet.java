package entities;

import java.util.List;
import java.util.Objects;

public class LogExerciseSet {
    private final String exerciseId;
    private final String exerciseName;
    private final MeasurementType measurementType;
    private final List<Double> weights;
    private final List<Integer> reps;
    private final List<Integer> times;
    private final List<Double> distances;

    public LogExerciseSet(String exerciseName, List<Double> weights, List<Integer> reps) {
        this(null, exerciseName, MeasurementType.WEIGHT_REPS,
                weights, reps, List.of(), List.of());
    }

    public LogExerciseSet(
            String exerciseId,
            String exerciseName,
            MeasurementType measurementType,
            List<Double> weights,
            List<Integer> reps,
            List<Integer> times,
            List<Double> distances
    ) {
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.measurementType = Objects.requireNonNull(measurementType, "measurementType");
        this.weights = List.copyOf(weights);
        this.reps = List.copyOf(reps);
        this.times = List.copyOf(times);
        this.distances = List.copyOf(distances);

        if (this.weights.size() != this.reps.size()) {
            throw new IllegalArgumentException("weights and reps must have the same number of sets");
        }
        if (this.times.size() != this.distances.size()) {
            throw new IllegalArgumentException("times and distances must have the same number of sets");
        }
        if (measurementType == MeasurementType.WEIGHT_REPS && !this.times.isEmpty()) {
            throw new IllegalArgumentException("WEIGHT_REPS requires empty time and distance lists");
        }
        if (measurementType == MeasurementType.TIME_DISTANCE && !this.weights.isEmpty()) {
            throw new IllegalArgumentException("TIME_DISTANCE requires empty weight and rep lists");
        }
    }

    public String getExerciseId() {
        return exerciseId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public MeasurementType getMeasurementType() {
        return measurementType;
    }

    public List<Double> getWeights() {
        return weights;
    }

    public List<Integer> getReps() {
        return reps;
    }

    public List<Integer> getTimes() {
        return times;
    }

    public List<Double> getDistances() {
        return distances;
    }

    public int getSetCount() {
        return measurementType == MeasurementType.TIME_DISTANCE
                ? times.size()
                : weights.size();
    }

    public double getWeight(int setNumber) {
        return weights.get(setNumber);
    }

    public int getReps(int setNumber) {
        return reps.get(setNumber);
    }

    public int getTime(int setNumber) {
        return times.get(setNumber);
    }

    public double getDistance(int setNumber) {
        return distances.get(setNumber);
    }
}