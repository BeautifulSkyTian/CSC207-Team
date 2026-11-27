package use_case.export_template;

/** Output boundary for the export template use case. */
public interface ExportTemplateOutputBoundary {
    void presentSuccess(ExportTemplateOutputData result);

    void presentFailure(String message);
}
