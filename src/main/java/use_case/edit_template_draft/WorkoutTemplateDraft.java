package use_case.edit_template_draft;

import entities.ExerciseSetGroup;
import entities.Template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable editor snapshot with no permanent template ID.
 * Blank names, empty groups, and unfinished set input are allowed.
 * Editing will produce a new snapshot rather than modifying this one.
 * Finished plans use the shared entities.Template and entities.ExerciseSetGroup.
 */
public final class WorkoutTemplateDraft {
    private final String templateName;
    private final List<DraftExerciseGroup> groups;

    public WorkoutTemplateDraft(String templateName, List<DraftExerciseGroup> groups) {
        this.templateName = Objects.requireNonNull(templateName, "templateName");
        this.groups = List.copyOf(groups);
        Set<UUID> rowIds = new HashSet<>();
        for (DraftExerciseGroup group : this.groups) {
            if (!rowIds.add(group.getRowId())) {
                throw new IllegalArgumentException("Each draft exercise occurrence must have a unique rowId");
            }
        }
    }

    /** Creates an independent draft from a shared template for a new editing session. */
    public static WorkoutTemplateDraft fromTemplate(Template template) {
        Objects.requireNonNull(template, "template");
        List<DraftExerciseGroup> draftGroups = new ArrayList<>();
        for (ExerciseSetGroup group : template.getGroups()) {
            draftGroups.add(DraftExerciseGroup.fromGroup(group));
        }
        return new WorkoutTemplateDraft(template.getName(), draftGroups);
    }

    public String getTemplateName() {
        return templateName;
    }

    public List<DraftExerciseGroup> getGroups() {
        return groups;
    }
}
