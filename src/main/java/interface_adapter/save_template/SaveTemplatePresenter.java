package interface_adapter.save_template;

import interface_adapter.create_template.TemplateEditorState;
import interface_adapter.create_template.TemplateEditorViewModel;
import use_case.edit_template_draft.WorkoutTemplateDraft;
import use_case.save_template.SaveTemplateOutputBoundary;
import use_case.save_template.SaveTemplateOutputData;

/** Shows save feedback and keeps the persisted template identity in the editor. */
public final class SaveTemplatePresenter implements SaveTemplateOutputBoundary {
    private final TemplateEditorViewModel viewModel;

    public SaveTemplatePresenter(TemplateEditorViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentSuccess(SaveTemplateOutputData result) {
        WorkoutTemplateDraft draft = viewModel.getState().getDraft();
        WorkoutTemplateDraft savedDraft = new WorkoutTemplateDraft(
                result.getTemplateId(), result.getTemplateName(), draft.getGroups());
        viewModel.updateState(new TemplateEditorState(savedDraft, "Template saved.", null));
    }

    @Override
    public void presentFailure(String message) {
        WorkoutTemplateDraft draft = viewModel.getState().getDraft();
        viewModel.updateState(new TemplateEditorState(draft, null, message));
    }
}
