package use_case.save_template;

import use_case.edit_template_draft.WorkoutTemplateDraft;

import java.util.Objects;

/** The editor snapshot at the time the user requests a save. */
public final class SaveTemplateInputData {
    private final WorkoutTemplateDraft draft;

    public SaveTemplateInputData(WorkoutTemplateDraft draft) {
        this.draft = Objects.requireNonNull(draft);
    }

    public WorkoutTemplateDraft getDraft() {
        return draft;
    }
}
