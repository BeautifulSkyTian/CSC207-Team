package interface_adapter.load_templates;

import entities.Template;
import interface_adapter.ViewManagerModel;
import interface_adapter.create_template.TemplateEditorViewModel;
import interface_adapter.home.HomeState;
import interface_adapter.home.HomeViewModel;
import use_case.load_templates.LoadTemplatesOutputBoundary;
import use_case.load_templates.LoadTemplatesOutputData;

/** Presenter for the load templates use case. */
public class LoadTemplatesPresenter implements LoadTemplatesOutputBoundary {
    private final HomeViewModel home;
    private final TemplateEditorViewModel editor;
    private final ViewManagerModel viewManager;

    public LoadTemplatesPresenter(HomeViewModel home, TemplateEditorViewModel editor,
                                  ViewManagerModel viewManager) {
        this.home = home;
        this.editor = editor;
        this.viewManager = viewManager;
    }

    @Override
    public void presentTemplates(LoadTemplatesOutputData data) {
        home.getState().setTemplateNames(data.getTemplateNames());
        home.getState().setError(null);
        home.firePropertyChange();
    }

    @Override
    public void presentTemplate(Template template) {
        editor.startEditing(template);
        viewManager.setState(editor.getViewName());
        viewManager.firePropertyChange();
    }

    @Override
    public void presentDeletion(LoadTemplatesOutputData data) {
        home.getState().setMessage("Template deleted.");
        presentTemplates(data);
    }

    @Override
    public void presentFailure(String message) {
        HomeState state = home.getState();
        state.setMessage(null);
        state.setError(message);
        home.firePropertyChange();
    }
}
