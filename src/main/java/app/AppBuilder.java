package app;

import interface_adapter.ViewManagerModel;
import interface_adapter.export_template.ExportTemplateController;
import interface_adapter.home.HomeViewModel;
import interface_adapter.import_template.ImportTemplateController;
import interface_adapter.load_templates.LoadTemplatesController;
import interface_adapter.start_workout.StartWorkoutController;
import interface_adapter.view_history.ViewHistoryController;
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
        // TODO: create the presenter and interactor and pass the interactor to the controller.
        homeView.setLoadTemplatesController(new LoadTemplatesController());
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

        viewManagerModel.setState(homeView.getViewName());
        viewManagerModel.firePropertyChange();
        homeView.loadTemplates();

        return frame;
    }
}
