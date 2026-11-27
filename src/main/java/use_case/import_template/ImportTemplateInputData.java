package use_case.import_template;

/** Input data for the import template use case. */
public class ImportTemplateInputData {
    private final String filePath;

    public ImportTemplateInputData(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}
