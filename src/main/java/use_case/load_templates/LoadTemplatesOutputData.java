package use_case.load_templates;

import java.util.List;

/** Names of saved templates in display order. */
public class LoadTemplatesOutputData {
    private final List<String> templateNames;

    public LoadTemplatesOutputData(List<String> templateNames) {
        this.templateNames = List.copyOf(templateNames);
    }

    public List<String> getTemplateNames() {
        return templateNames;
    }
}
