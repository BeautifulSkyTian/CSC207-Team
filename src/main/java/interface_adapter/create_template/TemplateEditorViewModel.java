package interface_adapter.create_template;

import interface_adapter.ViewModel;
import use_case.edit_template_draft.WorkoutTemplateDraft;

import java.util.List;

/** Shares the current template draft between the edit and save use cases. */
public final class TemplateEditorViewModel extends ViewModel<TemplateEditorState> {
    public static final String VIEW_NAME = "create template";

    public TemplateEditorViewModel() {
        super(VIEW_NAME, emptyState());
    }

    /** Starts a fresh editing session. */
    public void startNewDraft() {
        updateState(emptyState());
    }

    /** Publishes a replacement state to the template editor. */
    public void updateState(TemplateEditorState state) {
        setState(state);
        firePropertyChange();
    }

    private static TemplateEditorState emptyState() {
        return new TemplateEditorState(new WorkoutTemplateDraft("", List.of()), null, null);
    }
}
