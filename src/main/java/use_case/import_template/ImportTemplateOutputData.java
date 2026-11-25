package use_case.import_template;

import java.util.List;

/** Output data for the import template use case. */
public class ImportTemplateOutputData {
    private final String importedName;
    private final List<String> templateNames;

    public ImportTemplateOutputData(String importedName, List<String> templateNames) {
        this.importedName = importedName;
        this.templateNames = List.copyOf(templateNames);
    }

    public String getImportedName() {
        return importedName;
    }

    public List<String> getTemplateNames() {
        return templateNames;
    }
}
