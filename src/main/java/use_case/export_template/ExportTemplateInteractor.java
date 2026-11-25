package use_case.export_template;

import entities.Template;
import use_case.TemplateDataAccessInterface;

import java.io.UncheckedIOException;
import java.nio.file.InvalidPathException;

/** Interactor for the export template use case. */
public class ExportTemplateInteractor implements ExportTemplateInputBoundary {
    private final TemplateDataAccessInterface templates;
    private final ExportTemplateOutputBoundary presenter;

    public ExportTemplateInteractor(TemplateDataAccessInterface templates, ExportTemplateOutputBoundary presenter) {
        this.templates = templates;
        this.presenter = presenter;
    }

    @Override
    public void execute(ExportTemplateInputData input) {
        Template template = templates.get(input.getTemplateName());
        if (template == null) {
            presenter.presentFailure("This template is no longer available.");
            return;
        }

        try {
            templates.writeToFile(template, input.getFilePath());
        } catch (InvalidPathException e) {
            presenter.presentFailure("Enter a valid filename for the export.");
            return;
        } catch (IllegalArgumentException e) {
            presenter.presentFailure(e.getMessage());
            return;
        } catch (UncheckedIOException e) {
            presenter.presentFailure("Could not write the export file. Choose a writable destination.");
            return;
        }

        presenter.presentSuccess(new ExportTemplateOutputData(input.getFilePath()));
    }
}
