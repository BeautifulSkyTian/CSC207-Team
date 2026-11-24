package interface_adapter.save_template;

import interface_adapter.create_template.TemplateEditorViewModel;
import use_case.save_template.SaveTemplateInputBoundary;
import use_case.save_template.SaveTemplateInputData;

/** Submits the current template draft when the user requests a save. */
public final class SaveTemplateController {
    private final SaveTemplateInputBoundary interactor;
    private final TemplateEditorViewModel viewModel;

    public SaveTemplateController(SaveTemplateInputBoundary interactor,
                                  TemplateEditorViewModel viewModel) {
        this.interactor = interactor;
        this.viewModel = viewModel;
    }

    public void execute() {
        interactor.execute(new SaveTemplateInputData(viewModel.getState().getDraft()));
    }
}
