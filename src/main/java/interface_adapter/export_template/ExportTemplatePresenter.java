package interface_adapter.export_template;

import interface_adapter.home.HomeState;
import interface_adapter.home.HomeViewModel;
import use_case.export_template.ExportTemplateOutputBoundary;
import use_case.export_template.ExportTemplateOutputData;

/** Presenter for the export template use case. */
public class ExportTemplatePresenter implements ExportTemplateOutputBoundary {
    private final HomeViewModel home;

    public ExportTemplatePresenter(HomeViewModel home) {
        this.home = home;
    }

    @Override
    public void presentSuccess(ExportTemplateOutputData result) {
        HomeState state = home.getState();
        state.setError(null);
        state.setMessage("Template exported to " + result.getFilePath());
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
