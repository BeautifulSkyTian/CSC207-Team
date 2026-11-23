package entities.template_creation;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable finished plan for the template-creation use case.
 * Storage supplies templateId; this entity performs no ID allocation or I/O.
 * This model is separate from entities.Template used by the existing JSON DAO.
 */
public final class Template {
    private final int templateId;
    private final String templateName;
    private final List<ExerciseSetGroup> groups;

    public Template(int templateId, String templateName, List<ExerciseSetGroup> groups) {
        this.templateName = Objects.requireNonNull(templateName, "templateName");
        if (templateName.isBlank()) {
            throw new IllegalArgumentException("A finished template must have a name");
        }
        this.groups = List.copyOf(groups);
        if (this.groups.isEmpty()) {
            throw new IllegalArgumentException("A finished template must contain an exercise group");
        }
        Set<Integer> groupIds = new HashSet<>();
        for (ExerciseSetGroup group : this.groups) {
            if (!groupIds.add(group.getSetId())) {
                throw new IllegalArgumentException("Group setId values must be unique within a template");
            }
        }
        this.templateId = templateId;
    }

    public int getTemplateId() {
        return templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public List<ExerciseSetGroup> getGroups() {
        return groups;
    }

    /** Finds a group by its setId, rather than by its position in the list. */
    public ExerciseSetGroup getGroup(int groupId) {
        for (ExerciseSetGroup group : groups) {
            if (group.getSetId() == groupId) {
                return group;
            }
        }
        throw new IllegalArgumentException("No exercise group with ID " + groupId);
    }
}
