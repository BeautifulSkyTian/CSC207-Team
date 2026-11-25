package use_case.load_templates;

/** Input boundary for the load templates use case. */
public interface LoadTemplatesInputBoundary {
    void execute();

    void openTemplate(String name);
}
