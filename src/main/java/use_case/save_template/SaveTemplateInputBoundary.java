package use_case.save_template;

/** Accepts requests to save a workout template. */
public interface SaveTemplateInputBoundary {
    void execute(SaveTemplateInputData input);
}
