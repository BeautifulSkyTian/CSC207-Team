package use_case.load_templates;

import entities.Template;
import use_case.TemplateDataAccessInterface;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;

/** Supplies the home screen with templates and handles actions on saved templates. */
public class LoadTemplatesInteractor implements LoadTemplatesInputBoundary {
    private final TemplateDataAccessInterface templates;
    private final LoadTemplatesOutputBoundary presenter;

    public LoadTemplatesInteractor(TemplateDataAccessInterface templates,
                                   LoadTemplatesOutputBoundary presenter) {
        this.templates = templates;
        this.presenter = presenter;
    }

    @Override
    public void execute() {
        presenter.presentTemplates(loadTemplateNames());
    }

    @Override
    public void openTemplate(String name) {
        Template template = templates.get(name);
        if (template == null) {
            presenter.presentFailure("This template is no longer available.");
            return;
        }
        presenter.presentTemplate(template);
    }

    @Override
    public void deleteTemplate(String name) {
        try {
            if (!templates.delete(name)) {
                presenter.presentFailure("This template is no longer available.");
                return;
            }
        } catch (UncheckedIOException e) {
            presenter.presentFailure("Could not update the workout template file.");
            return;
        }
        presenter.presentDeletion(loadTemplateNames());
    }

    private LoadTemplatesOutputData loadTemplateNames() {
        List<String> names = templates.getAll().stream()
                .map(Template::getName)
                .collect(Collectors.toList());
        return new LoadTemplatesOutputData(names);
    }
}
