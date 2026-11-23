package use_case.edit_template_draft;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable editor snapshot with no permanent template ID.
 * Blank names, empty groups, and unfinished set input are allowed.
 * Editing will produce a new snapshot rather than modifying this one.
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

    public String getTemplateName() {
        return templateName;
    }

    public List<DraftExerciseGroup> getGroups() {
        return groups;
    }
}
