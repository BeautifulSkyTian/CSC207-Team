package use_case.edit_template_draft;

import entities.Exercise;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DraftModelsTest {
    private final Exercise exercise = new Exercise.ExerciseBuilder()
            .id("bench").name("Bench press").build();

    @Test
    void draftRetainsBlankNamesEmptyGroupsAndUnfinishedText() {
        WorkoutTemplateDraft empty = new WorkoutTemplateDraft("", List.of());
        assertEquals("", empty.getTemplateName());
        assertTrue(empty.getGroups().isEmpty());
        DraftSet set = new DraftSet("12.", "not yet entered", "", "  ");
        assertEquals("12.", set.getWeightText());
        assertEquals("not yet entered", set.getRepsText());
        assertEquals("", set.getTimeText());
        assertEquals("  ", set.getDistanceText());
        DraftExerciseGroup emptyGroup = new DraftExerciseGroup(UUID.randomUUID(), exercise, List.of());
        assertEquals(0, emptyGroup.getSetCount());
    }

    @Test
    void draftSnapshotsKeepOrderedGroupsAndSetsWhenInputListsChange() {
        DraftSet firstSet = new DraftSet("40", "10", "", "");
        DraftSet secondSet = new DraftSet("50", "8", "", "");
        List<DraftSet> inputSets = new ArrayList<>(List.of(firstSet, secondSet));
        UUID firstRow = UUID.randomUUID();
        DraftExerciseGroup first = new DraftExerciseGroup(firstRow, exercise, inputSets);
        DraftExerciseGroup second = new DraftExerciseGroup(UUID.randomUUID(), exercise, List.of());
        List<DraftExerciseGroup> inputGroups = new ArrayList<>(List.of(first, second));
        WorkoutTemplateDraft draft = new WorkoutTemplateDraft("Plan", inputGroups);
        inputSets.clear();
        inputGroups.clear();

        assertEquals("Plan", draft.getTemplateName());
        assertEquals(List.of(first, second), draft.getGroups());
        assertEquals(firstRow, first.getRowId());
        assertNotEquals(first.getRowId(), second.getRowId());
        assertSame(exercise, first.getExercise());
        assertSame(exercise, second.getExercise());
        assertEquals(List.of(firstSet, secondSet), first.getSets());
        assertEquals(2, first.getSetCount());
        assertThrows(UnsupportedOperationException.class, () -> draft.getGroups().clear());
        assertThrows(UnsupportedOperationException.class, () -> first.getSets().clear());
    }

    @Test
    void replacementSnapshotKeepsOccurrenceIdentityAndLeavesPreviousValuesAvailable() {
        UUID rowId = UUID.randomUUID();
        DraftSet originalSet = new DraftSet("40", "10", "", "");
        DraftExerciseGroup originalGroup = new DraftExerciseGroup(rowId, exercise, List.of(originalSet));
        WorkoutTemplateDraft original = new WorkoutTemplateDraft("Before", List.of(originalGroup));
        DraftSet replacement = new DraftSet("45", "8", "", "");
        DraftExerciseGroup updatedGroup = new DraftExerciseGroup(rowId, exercise, List.of(replacement));
        WorkoutTemplateDraft updated = new WorkoutTemplateDraft("After", List.of(updatedGroup));

        assertEquals(originalGroup.getRowId(), updatedGroup.getRowId());
        assertEquals("Before", original.getTemplateName());
        assertEquals("40", original.getGroups().get(0).getSets().get(0).getWeightText());
        assertEquals("After", updated.getTemplateName());
        assertEquals("45", updated.getGroups().get(0).getSets().get(0).getWeightText());
    }

    @Test
    void draftRejectsDuplicateOccurrenceIdsAndMissingRequiredStructure() {
        UUID rowId = UUID.randomUUID();
        DraftExerciseGroup first = new DraftExerciseGroup(rowId, exercise, List.of());
        DraftExerciseGroup duplicate = new DraftExerciseGroup(rowId, exercise, List.of());
        assertThrows(IllegalArgumentException.class,
                () -> new WorkoutTemplateDraft("Plan", List.of(first, duplicate)));
        assertThrows(NullPointerException.class, () -> new DraftExerciseGroup(null, exercise, List.of()));
        assertThrows(NullPointerException.class, () -> new DraftExerciseGroup(rowId, null, List.of()));
        assertThrows(NullPointerException.class, () -> new DraftSet(null, "", "", ""));
        assertThrows(NullPointerException.class, () -> new WorkoutTemplateDraft(null, List.of()));
    }
}
