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
 * Immutable editor snapshot. New drafts have no ID; snapshots of saved plans retain theirs.
 * Blank names, empty groups, and unfinished set input are allowed.
 * Editing will produce a new snapshot rather than modifying this one.
 * Finished plans use the shared entities.Template and entities.ExerciseSetGroup.
 */
public final class WorkoutTemplateDraft {
    private final String templateId;
    private final String templateName;
    private final List<DraftExerciseGroup> groups;

    public WorkoutTemplateDraft(String templateName, List<DraftExerciseGroup> groups) {
        this(null, templateName, groups);
    }

    public WorkoutTemplateDraft(String templateId, String templateName, List<DraftExerciseGroup> groups) {
        if (templateId != null && templateId.isBlank()) {
            throw new IllegalArgumentException("An existing template ID must not be blank");
        }
        this.templateId = templateId;
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
        return new WorkoutTemplateDraft(template.getTemplateId(), template.getName(), draftGroups);
    }

    /** Returns the existing template ID, or null for a new, unsaved draft. */
    public String getTemplateId() {
        return templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public List<DraftExerciseGroup> getGroups() {
        return groups;
    }
}
