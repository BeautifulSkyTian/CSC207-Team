package entities.template_creation;

import entities.Exercise;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemplateModelsTest {
    private final Exercise exercise = new Exercise.ExerciseBuilder()
            .id("bench").name("Bench press").build();

    @Test
    void templatePreservesOrderAndFindsRepeatedExercisesByGroupId() {
        ExerciseSetGroup first = group(20);
        ExerciseSetGroup second = group(10);
        List<ExerciseSetGroup> input = new ArrayList<>(List.of(first, second));
        Template template = new Template(7, "Push day", input);
        input.clear();

        assertEquals(7, template.getTemplateId());
        assertEquals("Push day", template.getTemplateName());
        assertEquals(List.of(first, second), template.getGroups());
        assertSame(second, template.getGroup(10));
        assertSame(first, template.getGroup(20));
        assertSame(exercise, first.getExercise());
        assertThrows(IllegalArgumentException.class, () -> template.getGroup(0));
        assertThrows(UnsupportedOperationException.class, () -> template.getGroups().clear());
    }

    @Test
    void finishedTemplateRejectsBlankNamesEmptyGroupsAndDuplicateGroupIds() {
        assertThrows(IllegalArgumentException.class, () -> new Template(1, " \t", List.of(group(1))));
        assertThrows(IllegalArgumentException.class, () -> new Template(1, "Plan", List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new Template(1, "Plan", List.of(group(1), group(1))));
    }

    @Test
    void measurementsAreAlignedImmutableSnapshots() {
        List<Double> weights = new ArrayList<>(List.of(40.0, 50.0));
        List<Integer> reps = new ArrayList<>(List.of(10, 8));
        List<Integer> time = new ArrayList<>(List.of(30, 40));
        List<Integer> distance = new ArrayList<>(List.of(100, 200));
        ExerciseSetGroup group = new ExerciseSetGroup(exercise, 12, weights, reps, time, distance);
        weights.clear();
        reps.clear();
        time.clear();
        distance.clear();

        assertEquals(12, group.getSetId());
        assertEquals(2, group.getSetCount());
        assertEquals(50.0, group.getWeight(1));
        assertEquals(8, group.getReps(1));
        assertEquals(40, group.getTime(1));
        assertEquals(200, group.getDistance(1));
        assertEquals(List.of(40.0, 50.0), group.getWeights());
        assertEquals(List.of(10, 8), group.getReps());
        assertEquals(List.of(30, 40), group.getTime());
        assertEquals(List.of(100, 200), group.getDistance());
        assertThrows(UnsupportedOperationException.class, () -> group.getWeights().clear());
        assertThrows(UnsupportedOperationException.class, () -> group.getReps().clear());
        assertThrows(UnsupportedOperationException.class, () -> group.getTime().clear());
        assertThrows(UnsupportedOperationException.class, () -> group.getDistance().clear());
        assertThrows(IndexOutOfBoundsException.class, () -> group.getWeight(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> group.getReps(2));
    }

    @Test
    void unusedMeasurementsAreEmptyAndSetCountCanComeFromTime() {
        ExerciseSetGroup timed = new ExerciseSetGroup(exercise, 1,
                List.of(), List.of(), List.of(30, 45), List.of());
        assertEquals(2, timed.getSetCount());
        assertTrue(timed.getWeights().isEmpty());
        assertTrue(timed.getReps().isEmpty());
        assertTrue(timed.getDistance().isEmpty());
        assertEquals(45, timed.getTime(1));
        assertEquals(0, new ExerciseSetGroup(exercise, 2,
                List.of(), List.of(), List.of(), List.of()).getSetCount());
    }

    @Test
    void nonemptyMeasurementsMustHaveMatchingLengths() {
        assertThrows(IllegalArgumentException.class, () -> new ExerciseSetGroup(exercise, 1,
                List.of(40.0, 50.0), List.of(10), List.of(), List.of()));
        assertThrows(IllegalArgumentException.class, () -> new ExerciseSetGroup(exercise, 1,
                List.of(), List.of(), List.of(30, 45), List.of(100)));
        assertThrows(NullPointerException.class, () -> new ExerciseSetGroup(null, 1,
                List.of(), List.of(), List.of(), List.of()));
    }

    private ExerciseSetGroup group(int id) {
        return new ExerciseSetGroup(exercise, id, List.of(40.0), List.of(10), List.of(), List.of());
    }
}
