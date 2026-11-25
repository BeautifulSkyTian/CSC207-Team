package use_case.export_template;

/** Input data for the export template use case. */
public class ExportTemplateInputData {
    private final String templateName;
    private final String filePath;

    public ExportTemplateInputData(String templateName, String filePath) {
        this.templateName = templateName;
        this.filePath = filePath;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getFilePath() {
        return filePath;
    }
}
