package use_case.save_template;

/** Reports the outcome of saving a workout template. */
public interface SaveTemplateOutputBoundary {
    void presentSuccess(SaveTemplateOutputData result);

    void presentFailure(String message);
}
