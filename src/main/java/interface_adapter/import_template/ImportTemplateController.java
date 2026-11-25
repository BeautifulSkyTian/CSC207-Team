package interface_adapter.import_template;

import use_case.import_template.ImportTemplateInputBoundary;
import use_case.import_template.ImportTemplateInputData;

/** Controller for the import template use case. */
public class ImportTemplateController {
    private final ImportTemplateInputBoundary interactor;

    public ImportTemplateController(ImportTemplateInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Imports a template from a JSON file.
     *
     * @param filePath the path of the JSON file to read
     */
    public void execute(String filePath) {
        interactor.execute(new ImportTemplateInputData(filePath));
    }
}
