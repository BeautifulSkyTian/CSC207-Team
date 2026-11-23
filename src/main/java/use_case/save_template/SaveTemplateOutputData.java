package use_case.save_template;

/** The identity and name of a successfully saved template. */
public final class SaveTemplateOutputData {
    private final String templateId;
    private final String templateName;

    public SaveTemplateOutputData(String templateId, String templateName) {
        this.templateId = templateId;
        this.templateName = templateName;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getTemplateName() {
        return templateName;
    }
}
