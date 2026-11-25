package interface_adapter.load_templates;

import use_case.load_templates.LoadTemplatesInputBoundary;

/** Controller for the load templates use case. */
public class LoadTemplatesController {
    private final LoadTemplatesInputBoundary interactor;

    public LoadTemplatesController(LoadTemplatesInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Loads the saved template names into the home screen.
     */
    public void execute() {
        interactor.execute();
    }

    /** Opens a saved template in the editor. */
    public void openTemplate(String name) {
        interactor.openTemplate(name);
    }
}
