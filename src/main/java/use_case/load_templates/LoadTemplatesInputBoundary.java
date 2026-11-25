package use_case.load_templates;

/** Actions on saved templates from the home screen. */
public interface LoadTemplatesInputBoundary {
    void execute();

    void openTemplate(String name);

    void deleteTemplate(String name);
}
