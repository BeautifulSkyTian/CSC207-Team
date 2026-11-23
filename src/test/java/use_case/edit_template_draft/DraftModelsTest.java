package use_case.edit_template_draft;

import entities.Exercise;
import entities.ExerciseSetGroup;
import entities.Template;
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
        DraftSet set = new DraftSet("12.", "  not yet entered  ");
        assertEquals("12.", set.getWeightText());
        assertEquals("  not yet entered  ", set.getRepsText());
        DraftSet blank = new DraftSet("", "");
        assertEquals("", blank.getWeightText());
        assertEquals("", blank.getRepsText());
        DraftExerciseGroup emptyGroup = new DraftExerciseGroup(UUID.randomUUID(), exercise, List.of());
        assertEquals(0, emptyGroup.getSetCount());
    }

    @Test
    void draftSnapshotsKeepOrderedGroupsAndSetsWhenInputListsChange() {
        DraftSet firstSet = new DraftSet("40", "10");
        DraftSet secondSet = new DraftSet("50", "8");
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
        DraftSet originalSet = new DraftSet("40", "10");
        DraftExerciseGroup originalGroup = new DraftExerciseGroup(rowId, exercise, List.of(originalSet));
        WorkoutTemplateDraft original = new WorkoutTemplateDraft("Before", List.of(originalGroup));
        DraftSet replacement = new DraftSet("45", "8");
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
        assertThrows(NullPointerException.class, () -> new DraftSet(null, ""));
        assertThrows(NullPointerException.class, () -> new DraftSet("", null));
        assertThrows(NullPointerException.class, () -> new WorkoutTemplateDraft(null, List.of()));
    }

    @Test
    void sharedTemplateBecomesAnIndependentOrderedDraftWithDistinctExerciseOccurrences() {
        ExerciseSetGroup first = new ExerciseSetGroup(exercise);
        first.addSet(40.5, 10);
        first.addSet(50.0, 8);
        ExerciseSetGroup second = new ExerciseSetGroup(exercise);
        second.addSet(30.0, 12);
        Template template = new Template("Push day");
        template.addGroup(first);
        template.addGroup(second);

        WorkoutTemplateDraft draft = WorkoutTemplateDraft.fromTemplate(template);
        assertEquals("Push day", draft.getTemplateName());
        assertEquals(2, draft.getGroups().size());
        DraftExerciseGroup firstDraft = draft.getGroups().get(0);
        DraftExerciseGroup secondDraft = draft.getGroups().get(1);
        assertSame(exercise, firstDraft.getExercise());
        assertSame(exercise, secondDraft.getExercise());
        assertNotEquals(firstDraft.getRowId(), secondDraft.getRowId());
        assertEquals(2, firstDraft.getSetCount());
        assertEquals("40.5", firstDraft.getSets().get(0).getWeightText());
        assertEquals("10", firstDraft.getSets().get(0).getRepsText());
        assertEquals("50.0", firstDraft.getSets().get(1).getWeightText());
        assertEquals("8", firstDraft.getSets().get(1).getRepsText());
        assertEquals("30.0", secondDraft.getSets().get(0).getWeightText());
        assertEquals("12", secondDraft.getSets().get(0).getRepsText());

        // Changing the mutable saved plan must not change the editor snapshot.
        template.setName("Changed");
        template.removeGroup(1);
        first.setWeight(0, 60.0);
        first.setReps(0, 5);
        first.removeSet(1);
        second.addSet(35.0, 10);
        assertEquals("Push day", draft.getTemplateName());
        assertEquals(2, draft.getGroups().size());
        assertEquals(2, firstDraft.getSetCount());
        assertEquals("40.5", firstDraft.getSets().get(0).getWeightText());
        assertEquals("10", firstDraft.getSets().get(0).getRepsText());
        assertEquals(1, secondDraft.getSetCount());
    }

    @Test
    void replacingADraftGroupDoesNotMutateTheSharedTemplate() {
        ExerciseSetGroup group = new ExerciseSetGroup(exercise);
        group.addSet(40.0, 10);
        Template template = new Template("Original");
        template.addGroup(group);
        WorkoutTemplateDraft draft = WorkoutTemplateDraft.fromTemplate(template);
        DraftExerciseGroup row = draft.getGroups().get(0);
        WorkoutTemplateDraft edited = new WorkoutTemplateDraft("Edited", List.of(
                new DraftExerciseGroup(row.getRowId(), row.getExercise(), List.of(new DraftSet("45", "8")))));

        assertEquals(row.getRowId(), edited.getGroups().get(0).getRowId());
        assertEquals("45", edited.getGroups().get(0).getSets().get(0).getWeightText());
        assertEquals("Original", template.getName());
        assertEquals(1, template.getGroupCount());
        assertEquals(List.of(40.0), group.getWeights());
        assertEquals(List.of(10), group.getRepsList());
    }

    @Test
    void sharedEmptyPlansAndGroupsCanBeSnapshottedWithoutInventingSets() {
        Template empty = new Template();
        WorkoutTemplateDraft emptyDraft = WorkoutTemplateDraft.fromTemplate(empty);
        assertEquals("", emptyDraft.getTemplateName());
        assertTrue(emptyDraft.getGroups().isEmpty());
        ExerciseSetGroup group = new ExerciseSetGroup(exercise);
        empty.addGroup(group);
        empty.addGroup(group);
        WorkoutTemplateDraft repeated = WorkoutTemplateDraft.fromTemplate(empty);
        assertEquals(2, repeated.getGroups().size());
        assertEquals(0, repeated.getGroups().get(0).getSetCount());
        assertEquals(0, repeated.getGroups().get(1).getSetCount());
        assertNotEquals(repeated.getGroups().get(0).getRowId(), repeated.getGroups().get(1).getRowId());
        assertThrows(NullPointerException.class, () -> WorkoutTemplateDraft.fromTemplate(null));
        assertThrows(NullPointerException.class, () -> DraftExerciseGroup.fromGroup(null));
    }
}
