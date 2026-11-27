package use_case.import_template;

import entities.Template;
import use_case.TemplateDataAccessInterface;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;

/** Interactor for the import template use case. */
public class ImportTemplateInteractor implements ImportTemplateInputBoundary {
    private final TemplateDataAccessInterface templates;
    private final ImportTemplateOutputBoundary presenter;

    public ImportTemplateInteractor(TemplateDataAccessInterface templates, ImportTemplateOutputBoundary presenter) {
        this.templates = templates;
        this.presenter = presenter;
    }

    @Override
    public void execute(ImportTemplateInputData input) {
        Template imported;
        try {
            imported = templates.readFromFile(input.getFilePath());
        } catch (UncheckedIOException e) {
            presenter.presentFailure("Could not read the template file.");
            return;
        } catch (IllegalArgumentException e) {
            presenter.presentFailure("The file is not a valid workout template.");
            return;
        }

        if (imported.getName().isBlank()) {
            presenter.presentFailure("The imported template must have a name.");
            return;
        }
        for (Template saved : templates.getAll()) {
            if (saved.getTemplateId().equals(imported.getTemplateId())) {
                presenter.presentFailure("This template is already saved as " + saved.getName() + ".");
                return;
            }
        }
        if (templates.existsByName(imported.getName())) {
            presenter.presentFailure("A template named " + imported.getName() + " already exists.");
            return;
        }

        try {
            templates.save(imported);
        } catch (UncheckedIOException e) {
            presenter.presentFailure("Could not save the imported template.");
            return;
        }
        List<String> templateNames = templates.getAll().stream()
                .map(Template::getName)
                .collect(Collectors.toList());
        presenter.presentSuccess(new ImportTemplateOutputData(imported.getName(), templateNames));
    }
}
