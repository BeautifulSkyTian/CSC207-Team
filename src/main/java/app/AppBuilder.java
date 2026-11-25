package app;

import data_access.JsonTemplateDataAccessObject;
import data_access.SearchExercise.ExternalExerciseApiGateway;
import data_access.SearchExercise.SearchExerciseDataAccessObject;
import interface_adapter.ViewManagerModel;
import interface_adapter.create_template.TemplateEditorState;
import interface_adapter.create_template.TemplateEditorViewModel;
import interface_adapter.edit_template_draft.EditTemplateDraftController;
import interface_adapter.edit_template_draft.EditTemplateDraftPresenter;
import interface_adapter.export_template.ExportTemplateController;
import interface_adapter.home.HomeState;
import interface_adapter.home.HomeViewModel;
import interface_adapter.import_template.ImportTemplateController;
import interface_adapter.load_templates.LoadTemplatesController;
import interface_adapter.load_templates.LoadTemplatesPresenter;
import interface_adapter.save_template.SaveTemplateController;
import interface_adapter.save_template.SaveTemplatePresenter;
import interface_adapter.start_workout.StartWorkoutController;
import interface_adapter.view_history.ViewHistoryController;
import use_case.TemplateDataAccessInterface;
import use_case.edit_template_draft.EditTemplateDraftInteractor;
import use_case.load_templates.LoadTemplatesInteractor;
import use_case.save_template.SaveTemplateInteractor;
import view.CreateTemplateView;
import view.HomeView;
import view.ViewManager;

import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * Creates and wires every view, view model, controller, presenter, interactor and data access object.
 *
 * <p>Call the add methods in order, then {@link #build()}:
 * <pre>
 * new AppBuilder()
 *         .addHomeView()
 *         .addLoadTemplatesUseCase()
 *         ...
 *         .build();
 * </pre>
 * The use case methods need the home view, so {@link #addHomeView()} must come first.
 */
public class AppBuilder {

    private static final String WINDOW_TITLE = "Workout Tracker";
    private static final int WINDOW_WIDTH = 480;
    private static final int WINDOW_HEIGHT = 600;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final ViewManagerModel viewManagerModel = new ViewManagerModel();
    private final TemplateEditorViewModel templateEditorViewModel = new TemplateEditorViewModel();
    private final TemplateDataAccessInterface templates = new JsonTemplateDataAccessObject();

    private HomeViewModel homeViewModel;
    private HomeView homeView;

    /** Creates a builder whose window shows the view named by its view manager model. */
    public AppBuilder() {
        new ViewManager(cardPanel, cardLayout, viewManagerModel);
    }

    /**
     * Adds the home screen.
     *
     * @return this builder
     */
    public AppBuilder addHomeView() {
        homeViewModel = new HomeViewModel();
        homeView = new HomeView(homeViewModel);
        cardPanel.add(homeView, homeView.getViewName());
        return this;
    }

    /**
     * Adds the load templates use case, used to fill the home screen's list.
     *
     * @return this builder
     */
    public AppBuilder addLoadTemplatesUseCase() {
        LoadTemplatesPresenter presenter =
                new LoadTemplatesPresenter(homeViewModel, templateEditorViewModel, viewManagerModel);
        LoadTemplatesInteractor interactor = new LoadTemplatesInteractor(templates, presenter);
        homeView.setLoadTemplatesController(new LoadTemplatesController(interactor));
        return this;
    }

    /** Adds the shared template editor and wires it to the home screen. */
    public AppBuilder addTemplateEditor() {
        EditTemplateDraftPresenter editPresenter = new EditTemplateDraftPresenter(templateEditorViewModel);
        EditTemplateDraftController editController = new EditTemplateDraftController(
                new EditTemplateDraftInteractor(editPresenter), templateEditorViewModel);
        SaveTemplatePresenter savePresenter = new SaveTemplatePresenter(templateEditorViewModel);
        SaveTemplateController saveController = new SaveTemplateController(
                new SaveTemplateInteractor(templates, savePresenter), templateEditorViewModel);
        CreateTemplateView editor = new CreateTemplateView(
                templateEditorViewModel, editController, saveController,
                () -> new SearchExerciseDataAccessObject(new ExternalExerciseApiGateway()).fetchAllExercises(),
                () -> showView(homeView.getViewName()));
        cardPanel.add(editor, editor.getViewName());

        homeView.setAddTemplateAction(() -> {
            templateEditorViewModel.startNewDraft();
            showView(editor.getViewName());
        });
        templateEditorViewModel.addPropertyChangeListener(event -> {
            TemplateEditorState state = (TemplateEditorState) event.getNewValue();
            if (state.getMessage() != null) {
                HomeState homeState = homeViewModel.getState();
                homeState.setSelectedTemplateName(state.getDraft().getTemplateName());
                homeState.setMessage(state.getMessage());
                homeView.loadTemplates();
                showView(homeView.getViewName());
            }
        });
        return this;
    }

    /**
     * Adds the start workout use case, run by the Start Workout button.
     *
     * @return this builder
     */
    public AppBuilder addStartWorkoutUseCase() {
        // TODO: create the presenter and interactor and pass the interactor to the controller.
        homeView.setStartWorkoutController(new StartWorkoutController());
        return this;
    }

    /**
     * Adds the view history use case, run by the History button.
     *
     * @return this builder
     */
    public AppBuilder addViewHistoryUseCase() {
        // TODO: create the presenter and interactor and pass the interactor to the controller.
        homeView.setViewHistoryController(new ViewHistoryController());
        return this;
    }

    /**
     * Adds the import template use case, run by the Import button.
     *
     * @return this builder
     */
    public AppBuilder addImportTemplateUseCase() {
        // TODO: create the presenter and interactor and pass the interactor to the controller.
        homeView.setImportTemplateController(new ImportTemplateController());
        return this;
    }

    /**
     * Adds the export template use case, run by the Export button.
     *
     * @return this builder
     */
    public AppBuilder addExportTemplateUseCase() {
        // TODO: create the presenter and interactor and pass the interactor to the controller.
        homeView.setExportTemplateController(new ExportTemplateController());
        return this;
    }

    /**
     * Builds the main window, opens it on the home screen and loads the saved templates.
     *
     * @return the main window, not yet visible
     */
    public JFrame build() {
        JFrame frame = new JFrame(WINDOW_TITLE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(cardPanel);
        frame.setMinimumSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        frame.pack();
        frame.setLocationRelativeTo(null);

        showView(homeView.getViewName());
        homeView.loadTemplates();

        return frame;
    }

    private void showView(String name) {
        viewManagerModel.setState(name);
        viewManagerModel.firePropertyChange();
    }
}
