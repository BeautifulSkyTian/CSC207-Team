package interface_adapter.home;

import java.util.ArrayList;
import java.util.List;

/** Home screen state: template names, selected template, message and error. */
public class HomeState {

    private List<String> templateNames = new ArrayList<>();
    private String selectedTemplateName;
    private String message;
    private String error;

    /** @return the names of the saved templates, in display order */
    public List<String> getTemplateNames() {
        return new ArrayList<>(templateNames);
    }

    /** @param templateNames the names of the saved templates, in display order */
    public void setTemplateNames(List<String> templateNames) {
        this.templateNames = new ArrayList<>(templateNames);
    }

    /** @return the name of the selected template, or null if none is selected */
    public String getSelectedTemplateName() {
        return selectedTemplateName;
    }

    /** @param selectedTemplateName the name of the selected template, or null for none */
    public void setSelectedTemplateName(String selectedTemplateName) {
        this.selectedTemplateName = selectedTemplateName;
    }

    /** @return the latest success message, or null if there is none */
    public String getMessage() {
        return message;
    }

    /** @param message the latest success message, or null to clear it */
    public void setMessage(String message) {
        this.message = message;
    }

    /** @return the latest error message, or null if there is none */
    public String getError() {
        return error;
    }

    /** @param error the latest error message, or null to clear it */
    public void setError(String error) {
        this.error = error;
    }
}
