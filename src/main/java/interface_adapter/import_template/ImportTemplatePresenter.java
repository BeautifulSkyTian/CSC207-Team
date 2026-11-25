package interface_adapter.import_template;

import interface_adapter.home.HomeState;
import interface_adapter.home.HomeViewModel;
import use_case.import_template.ImportTemplateOutputBoundary;
import use_case.import_template.ImportTemplateOutputData;

/** Presenter for the import template use case. */
public class ImportTemplatePresenter implements ImportTemplateOutputBoundary {
    private final HomeViewModel home;

    public ImportTemplatePresenter(HomeViewModel home) {
        this.home = home;
    }

    @Override
    public void presentSuccess(ImportTemplateOutputData result) {
        HomeState state = home.getState();
        state.setError(null);
        state.setMessage("Imported " + result.getImportedName() + ".");
        state.setTemplateNames(result.getTemplateNames());
        state.setSelectedTemplateName(result.getImportedName());
        home.firePropertyChange();
    }

    @Override
    public void presentFailure(String message) {
        HomeState state = home.getState();
        state.setError(message);
        state.setMessage(null);
        home.firePropertyChange();
    }
}
