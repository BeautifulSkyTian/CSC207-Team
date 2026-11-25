package use_case.load_templates;

import entities.Template;
import use_case.TemplateDataAccessInterface;

import java.util.List;
import java.util.stream.Collectors;

/** Supplies the home screen with template names and loads a saved template for editing. */
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
        List<String> names = templates.getAll().stream()
                .map(Template::getName)
                .collect(Collectors.toList());
        presenter.presentTemplates(new LoadTemplatesOutputData(names));
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
}
