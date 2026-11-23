package entities;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemplateTest {
    @Test
    void generatedIdsAreNonblankUniqueAndSurviveRenaming() {
        Template template = new Template("Before");
        String id = template.getTemplateId();
        assertFalse(id.isBlank());
        assertNotEquals(id, new Template("Before").getTemplateId());
        template.setName("After");
        assertEquals(id, template.getTemplateId());
        assertEquals("After", template.getName());
        assertEquals("", new Template().getName());
    }

    @Test
    void restoredIdsAndOrderedMixedGroupsArePreserved() {
        Exercise exercise = new Exercise.ExerciseBuilder().id("one").name("Exercise").build();
        ExerciseSetGroup weights = new ExerciseSetGroup(exercise, MeasurementType.WEIGHT_REPS);
        ExerciseSetGroup timed = new ExerciseSetGroup(exercise, MeasurementType.TIME_DISTANCE);
        Template template = new Template("template-1", "Mixed plan");
        template.addGroup(weights);
        template.addGroup(timed);
        assertEquals("template-1", template.getTemplateId());
        assertEquals(2, template.getGroupCount());
        assertEquals(List.of(weights, timed), template.getGroups());
        assertThrows(UnsupportedOperationException.class, () -> template.getGroups().clear());
        template.removeGroup(0);
        assertSame(timed, template.getGroup(0));
        assertEquals(1, template.getGroupCount());
    }

    @Test
    void missingIdentityOrStructureIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Template(" ", "Plan"));
        assertThrows(NullPointerException.class, () -> new Template(null, "Plan"));
        assertThrows(NullPointerException.class, () -> new Template("id", null));
        Template template = new Template("Plan");
        assertThrows(NullPointerException.class, () -> template.addGroup(null));
        assertThrows(NullPointerException.class, () -> template.setName(null));
        assertEquals("Plan", template.getName());
        assertEquals(0, template.getGroupCount());
    }
}
