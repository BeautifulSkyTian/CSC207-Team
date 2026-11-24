package interface_adapter.create_template;

import use_case.edit_template_draft.WorkoutTemplateDraft;

/** The current draft and the feedback to display in the template editor. */
public final class TemplateEditorState {
    private final WorkoutTemplateDraft draft;
    private final String message;
    private final String error;

    public TemplateEditorState(WorkoutTemplateDraft draft, String message, String error) {
        this.draft = draft;
        this.message = message;
        this.error = error;
    }

    public WorkoutTemplateDraft getDraft() {
        return draft;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }
}
