package interface_adapter.export_template;

import use_case.export_template.ExportTemplateInputBoundary;
import use_case.export_template.ExportTemplateInputData;

/** Controller for the export template use case. */
public class ExportTemplateController {
    private final ExportTemplateInputBoundary interactor;

    public ExportTemplateController(ExportTemplateInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Exports a template to a JSON file.
     *
     * @param templateName the name of the template to export
     * @param filePath the path of the JSON file to write
     */
    public void execute(String templateName, String filePath) {
        interactor.execute(new ExportTemplateInputData(templateName, filePath));
    }
}
