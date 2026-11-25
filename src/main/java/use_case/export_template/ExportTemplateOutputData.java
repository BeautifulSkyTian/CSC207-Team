package use_case.export_template;

/** Output data for the export template use case. */
public class ExportTemplateOutputData {
    private final String filePath;

    public ExportTemplateOutputData(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}
