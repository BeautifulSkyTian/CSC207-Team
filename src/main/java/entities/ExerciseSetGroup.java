package entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A group of sets for a single exercise within a template, e.g. "Bench Press: 3 sets".
 * Every set uses the group's measurement type: weight/reps or time/distance.
 * Unused measurement lists stay empty; the active pair has setCount elements.
 */
public class ExerciseSetGroup {
    private final Exercise exercise;
    private final MeasurementType measurementType;
    private int setCount = 0;
    private final List<Double> weights = new ArrayList<>();
    private final List<Integer> reps = new ArrayList<>();
    private final List<Integer> times = new ArrayList<>();
    private final List<Double> distances = new ArrayList<>();

    /** Compatibility constructor for existing weight/rep callers. */
    public ExerciseSetGroup(Exercise exercise){
        this(exercise, MeasurementType.WEIGHT_REPS);
    }

    public ExerciseSetGroup(Exercise exercise, MeasurementType measurementType){
        this.exercise = Objects.requireNonNull(exercise, "exercise");
        this.measurementType = Objects.requireNonNull(measurementType, "measurementType");
    }

    /**
     * Creates a deep copy of another group, so that changes to the copy
     * (e.g. during an ongoing workout) do not affect the original template.
     */
    public ExerciseSetGroup(ExerciseSetGroup other){
        this(other.exercise, other.measurementType);
        this.setCount = other.setCount;
        this.weights.addAll(other.weights);
        this.reps.addAll(other.reps);
        this.times.addAll(other.times);
        this.distances.addAll(other.distances);
    }

    /** Compatibility method for existing weight/rep callers. */
    public void addSet(double weight, int reps){
        addWeightRepSet(weight, reps);
    }

    public void addWeightRepSet(double weight, int reps){
        requireType(MeasurementType.WEIGHT_REPS);
        requireFinite(weight);
        this.weights.add(weight);
        this.reps.add(reps);
        this.setCount++;
    }

    public void addTimeDistanceSet(int time, double distance){
        requireType(MeasurementType.TIME_DISTANCE);
        requireFinite(distance);
        this.times.add(time);
        this.distances.add(distance);
        this.setCount++;
    }

    public void removeSet(int index){
        Objects.checkIndex(index, setCount);
        if (measurementType == MeasurementType.WEIGHT_REPS) {
            this.weights.remove(index);
            this.reps.remove(index);
        } else {
            this.times.remove(index);
            this.distances.remove(index);
        }
        this.setCount--;
    }

    public Exercise getExercise(){
        return this.exercise;
    }

    public MeasurementType getMeasurementType(){
        return measurementType;
    }

    public int getSetCount(){
        return setCount;
    }

    public double getWeight(int setNumber){
        requireType(MeasurementType.WEIGHT_REPS);
        return this.weights.get(setNumber);
    }

    public int getReps(int setNumber){
        requireType(MeasurementType.WEIGHT_REPS);
        return this.reps.get(setNumber);
    }

    public void setWeight(int setNumber, double weight){
        requireType(MeasurementType.WEIGHT_REPS);
        requireFinite(weight);
        this.weights.set(setNumber, weight);
    }

    public void setReps(int setNumber, int reps){
        requireType(MeasurementType.WEIGHT_REPS);
        this.reps.set(setNumber, reps);
    }

    public int getTime(int setNumber){
        requireType(MeasurementType.TIME_DISTANCE);
        return times.get(setNumber);
    }

    public double getDistance(int setNumber){
        requireType(MeasurementType.TIME_DISTANCE);
        return distances.get(setNumber);
    }

    public void setTime(int setNumber, int time){
        requireType(MeasurementType.TIME_DISTANCE);
        times.set(setNumber, time);
    }

    public void setDistance(int setNumber, double distance){
        requireType(MeasurementType.TIME_DISTANCE);
        requireFinite(distance);
        distances.set(setNumber, distance);
    }

    public List<Double> getWeights(){
        return List.copyOf(this.weights);
    }

    public List<Integer> getRepsList(){
        return List.copyOf(this.reps);
    }

    public List<Integer> getTimes(){
        return List.copyOf(times);
    }

    public List<Double> getDistances(){
        return List.copyOf(distances);
    }

    private void requireType(MeasurementType expected){
        if (measurementType != expected) {
            throw new IllegalStateException("Group uses " + measurementType + ", not " + expected);
        }
    }

    private static void requireFinite(double value){
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Measurements must be finite numbers");
        }
    }
}
