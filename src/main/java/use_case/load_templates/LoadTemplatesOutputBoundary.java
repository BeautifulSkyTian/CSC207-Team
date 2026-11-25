package use_case.load_templates;

import entities.Template;

/** Output boundary for the load templates use case. */
public interface LoadTemplatesOutputBoundary {
    void presentTemplates(LoadTemplatesOutputData data);

    void presentTemplate(Template template);

    void presentFailure(String message);
}
