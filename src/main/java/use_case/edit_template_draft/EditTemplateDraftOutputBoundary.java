package use_case.edit_template_draft;

/** Reports the outcome of a draft edit to the presenter. */
public interface EditTemplateDraftOutputBoundary {
    void presentDraft(WorkoutTemplateDraft updatedDraft);

    void presentFailure(String message);
}
