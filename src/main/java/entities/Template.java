package entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** A reusable workout plan with a stable identity independent of its editable name. */
public class Template {
    private final String templateId;
    private String templateName;
    private final List<ExerciseSetGroup> groups = new ArrayList<>();

    public Template(){
        this("");
    }

    public Template(String name){
        this(UUID.randomUUID().toString(), name);
    }

    /** Restores an existing identity, for example when loading a saved template. */
    public Template(String templateId, String name){
        this.templateId = Objects.requireNonNull(templateId, "templateId");
        if (templateId.isBlank()) {
            throw new IllegalArgumentException("templateId must not be blank");
        }
        this.templateName = Objects.requireNonNull(name, "name");
    }

    public String getTemplateId(){
        return templateId;
    }

    public String getName(){
        return templateName;
    }

    public void setName(String name){
        this.templateName = Objects.requireNonNull(name, "name");
    }

    public void addGroup(ExerciseSetGroup group){
        this.groups.add(Objects.requireNonNull(group, "group"));
    }

    public void removeGroup(int index){
        this.groups.remove(index);
    }

    public ExerciseSetGroup getGroup(int index){
        return this.groups.get(index);
    }

    public int getGroupCount(){
        return this.groups.size();
    }

    public List<ExerciseSetGroup> getGroups(){
        return List.copyOf(this.groups);
    }
}
