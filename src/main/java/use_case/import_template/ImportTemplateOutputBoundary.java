package use_case.import_template;

/** Output boundary for the import template use case. */
public interface ImportTemplateOutputBoundary {
    void presentSuccess(ImportTemplateOutputData result);

    void presentFailure(String message);
}
