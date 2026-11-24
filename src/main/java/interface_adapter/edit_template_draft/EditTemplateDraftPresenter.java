package interface_adapter.edit_template_draft;

import interface_adapter.create_template.TemplateEditorState;
import interface_adapter.create_template.TemplateEditorViewModel;
import use_case.edit_template_draft.EditTemplateDraftOutputBoundary;
import use_case.edit_template_draft.WorkoutTemplateDraft;

/** Publishes draft edits and editing errors to the template editor. */
public final class EditTemplateDraftPresenter implements EditTemplateDraftOutputBoundary {
    private final TemplateEditorViewModel viewModel;

    public EditTemplateDraftPresenter(TemplateEditorViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentDraft(WorkoutTemplateDraft updatedDraft) {
        viewModel.updateState(new TemplateEditorState(updatedDraft, null, null));
    }

    @Override
    public void presentFailure(String message) {
        WorkoutTemplateDraft draft = viewModel.getState().getDraft();
        viewModel.updateState(new TemplateEditorState(draft, null, message));
    }
}
