package entities;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExerciseSetGroupTest {
    private final Exercise exercise = new Exercise.ExerciseBuilder().id("run").name("Run").build();

    @Test
    void weightRepSetsKeepTheirOrderAndUnusedMeasurementsEmpty() {
        ExerciseSetGroup group = new ExerciseSetGroup(exercise, MeasurementType.WEIGHT_REPS);
        group.addWeightRepSet(40.0, 10);
        group.addWeightRepSet(50.0, 8);
        group.setWeight(1, 55.5);
        group.setReps(1, 7);
        group.removeSet(0);
        assertSame(exercise, group.getExercise());
        assertEquals(MeasurementType.WEIGHT_REPS, group.getMeasurementType());
        assertEquals(1, group.getSetCount());
        assertEquals(55.5, group.getWeight(0));
        assertEquals(7, group.getReps(0));
        assertEquals(List.of(55.5), group.getWeights());
        assertEquals(List.of(7), group.getRepsList());
        assertTrue(group.getTimes().isEmpty());
        assertTrue(group.getDistances().isEmpty());
        group.removeSet(0);
        assertEquals(0, group.getSetCount());
        assertTrue(group.getWeights().isEmpty());
        assertTrue(group.getRepsList().isEmpty());
    }

    @Test
    void timeDistanceSetsKeepTheirOrderAndUnusedMeasurementsEmpty() {
        ExerciseSetGroup group = new ExerciseSetGroup(exercise, MeasurementType.TIME_DISTANCE);
        group.addTimeDistanceSet(60, 0.25);
        group.addTimeDistanceSet(120, 0.5);
        group.setTime(1, 150);
        group.setDistance(1, 0.75);
        group.removeSet(0);
        assertEquals(MeasurementType.TIME_DISTANCE, group.getMeasurementType());
        assertEquals(1, group.getSetCount());
        assertEquals(150, group.getTime(0));
        assertEquals(0.75, group.getDistance(0));
        assertEquals(List.of(150), group.getTimes());
        assertEquals(List.of(0.75), group.getDistances());
        assertTrue(group.getWeights().isEmpty());
        assertTrue(group.getRepsList().isEmpty());
        group.removeSet(0);
        assertEquals(0, group.getSetCount());
        assertTrue(group.getTimes().isEmpty());
        assertTrue(group.getDistances().isEmpty());
    }

    @Test
    void wrongMeasurementOperationsFailWithoutChangingSets() {
        ExerciseSetGroup weights = new ExerciseSetGroup(exercise);
        weights.addSet(40.0, 10);
        assertThrows(IllegalStateException.class, () -> weights.addTimeDistanceSet(60, 0.25));
        assertThrows(IllegalStateException.class, () -> weights.setTime(0, 60));
        assertThrows(IllegalStateException.class, () -> weights.setDistance(0, 0.25));
        assertThrows(IllegalStateException.class, () -> weights.getTime(0));
        assertThrows(IllegalStateException.class, () -> weights.getDistance(0));
        assertEquals(List.of(40.0), weights.getWeights());
        assertEquals(List.of(10), weights.getRepsList());
        assertEquals(1, weights.getSetCount());

        ExerciseSetGroup timed = new ExerciseSetGroup(exercise, MeasurementType.TIME_DISTANCE);
        timed.addTimeDistanceSet(60, 0.25);
        assertThrows(IllegalStateException.class, () -> timed.addWeightRepSet(40.0, 10));
        assertThrows(IllegalStateException.class, () -> timed.addSet(40.0, 10));
        assertThrows(IllegalStateException.class, () -> timed.setWeight(0, 40.0));
        assertThrows(IllegalStateException.class, () -> timed.setReps(0, 10));
        assertThrows(IllegalStateException.class, () -> timed.getWeight(0));
        assertThrows(IllegalStateException.class, () -> timed.getReps(0));
        assertEquals(List.of(60), timed.getTimes());
        assertEquals(List.of(0.25), timed.getDistances());
        assertEquals(1, timed.getSetCount());
    }

    @Test
    void copiesAndReadOnlySnapshotsAreIndependentForBothMeasurementTypes() {
        for (MeasurementType type : MeasurementType.values()) {
            ExerciseSetGroup original = new ExerciseSetGroup(exercise, type);
            if (type == MeasurementType.WEIGHT_REPS) {
                original.addWeightRepSet(40.0, 10);
            } else {
                original.addTimeDistanceSet(60, 0.25);
            }
            ExerciseSetGroup copy = new ExerciseSetGroup(original);
            assertSame(exercise, copy.getExercise());
            assertEquals(type, copy.getMeasurementType());
            List<Double> weights = original.getWeights();
            List<Integer> reps = original.getRepsList();
            List<Integer> times = original.getTimes();
            List<Double> distances = original.getDistances();
            assertThrows(UnsupportedOperationException.class, () -> weights.add(1.0));
            assertThrows(UnsupportedOperationException.class, () -> reps.add(1));
            assertThrows(UnsupportedOperationException.class, () -> times.add(1));
            assertThrows(UnsupportedOperationException.class, () -> distances.add(1.0));
            copy.removeSet(0);
            assertEquals(1, original.getSetCount());
            assertEquals(0, copy.getSetCount());
            original.removeSet(0);
            assertEquals(type == MeasurementType.WEIGHT_REPS ? List.of(40.0) : List.of(), weights);
            assertEquals(type == MeasurementType.WEIGHT_REPS ? List.of(10) : List.of(), reps);
            assertEquals(type == MeasurementType.TIME_DISTANCE ? List.of(60) : List.of(), times);
            assertEquals(type == MeasurementType.TIME_DISTANCE ? List.of(0.25) : List.of(), distances);
        }
    }

    @Test
    void invalidSetIndicesPreserveBothActiveLists() {
        for (MeasurementType type : MeasurementType.values()) {
            ExerciseSetGroup group = new ExerciseSetGroup(exercise, type);
            if (type == MeasurementType.WEIGHT_REPS) {
                group.addWeightRepSet(40.0, 10);
            } else {
                group.addTimeDistanceSet(60, 0.25);
            }
            for (int index : new int[]{-1, 1}) {
                assertThrows(IndexOutOfBoundsException.class, () -> group.removeSet(index));
                if (type == MeasurementType.WEIGHT_REPS) {
                    assertThrows(IndexOutOfBoundsException.class, () -> group.setWeight(index, 50.0));
                    assertThrows(IndexOutOfBoundsException.class, () -> group.setReps(index, 8));
                } else {
                    assertThrows(IndexOutOfBoundsException.class, () -> group.setTime(index, 90));
                    assertThrows(IndexOutOfBoundsException.class, () -> group.setDistance(index, 0.5));
                }
            }
            assertEquals(1, group.getSetCount());
            if (type == MeasurementType.WEIGHT_REPS) {
                assertEquals(List.of(40.0), group.getWeights());
                assertEquals(List.of(10), group.getRepsList());
            } else {
                assertEquals(List.of(60), group.getTimes());
                assertEquals(List.of(0.25), group.getDistances());
            }
        }
    }

    @Test
    void nonfiniteMeasurementsAreRejectedBeforeChangingState() {
        ExerciseSetGroup weights = new ExerciseSetGroup(exercise);
        weights.addSet(40.0, 10);
        ExerciseSetGroup timed = new ExerciseSetGroup(exercise, MeasurementType.TIME_DISTANCE);
        timed.addTimeDistanceSet(60, 0.25);
        for (double invalid : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> weights.addWeightRepSet(invalid, 10));
            assertThrows(IllegalArgumentException.class, () -> weights.setWeight(0, invalid));
            assertThrows(IllegalArgumentException.class, () -> timed.addTimeDistanceSet(60, invalid));
            assertThrows(IllegalArgumentException.class, () -> timed.setDistance(0, invalid));
        }
        assertEquals(1, weights.getSetCount());
        assertEquals(List.of(40.0), weights.getWeights());
        assertEquals(List.of(10), weights.getRepsList());
        assertEquals(1, timed.getSetCount());
        assertEquals(List.of(60), timed.getTimes());
        assertEquals(List.of(0.25), timed.getDistances());
    }
}
