package view;

import interface_adapter.export_template.ExportTemplateController;
import interface_adapter.home.HomeState;
import interface_adapter.home.HomeViewModel;
import interface_adapter.import_template.ImportTemplateController;
import interface_adapter.load_templates.LoadTemplatesController;
import interface_adapter.start_workout.StartWorkoutController;
import interface_adapter.view_history.ViewHistoryController;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Home screen: template list, Start Workout, History, Import and Export. */
public class HomeView extends JPanel implements PropertyChangeListener {

    private static final String JSON_EXTENSION = "json";
    private static final Color ERROR_COLOR = new Color(176, 0, 32);

    private final HomeViewModel homeViewModel;

    private final DefaultListModel<String> templateListModel = new DefaultListModel<>();
    private final JList<String> templateList = new JList<>(templateListModel);

    private final JButton startWorkoutButton = new JButton(HomeViewModel.START_WORKOUT_BUTTON_LABEL);
    private final JButton historyButton = new JButton(HomeViewModel.HISTORY_BUTTON_LABEL);
    private final JButton importButton = new JButton(HomeViewModel.IMPORT_BUTTON_LABEL);
    private final JButton exportButton = new JButton(HomeViewModel.EXPORT_BUTTON_LABEL);
    private final JLabel statusLabel = new JLabel(" ");

    /** True while the list is being refilled from the state, so selection events are ignored. */
    private boolean updatingList;

    private LoadTemplatesController loadTemplatesController;
    private StartWorkoutController startWorkoutController;
    private ViewHistoryController viewHistoryController;
    private ImportTemplateController importTemplateController;
    private ExportTemplateController exportTemplateController;

    /**
     * Creates the home view and starts listening to its view model.
     *
     * @param homeViewModel the view model for this view
     */
    public HomeView(HomeViewModel homeViewModel) {
        this.homeViewModel = homeViewModel;
        homeViewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel(HomeViewModel.TITLE_LABEL);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        add(title, BorderLayout.NORTH);

        templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templateList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !updatingList) {
                onTemplateSelected(templateList.getSelectedValue());
            }
        });
        add(new JScrollPane(templateList), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttons.add(startWorkoutButton);
        buttons.add(historyButton);
        buttons.add(importButton);
        buttons.add(exportButton);

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.add(buttons, BorderLayout.NORTH);
        bottom.add(statusLabel, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        startWorkoutButton.addActionListener(e -> onStartWorkout());
        historyButton.addActionListener(e -> viewHistoryController.execute());
        importButton.addActionListener(e -> onImport());
        exportButton.addActionListener(e -> onExport());

        updateView(homeViewModel.getState());
    }

    /** @return the name this view is registered under in the ViewManager */
    public String getViewName() {
        return homeViewModel.getViewName();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateView((HomeState) evt.getNewValue());
    }

    /** Asks the load templates use case to fill the template list. */
    public void loadTemplates() {
        loadTemplatesController.execute();
    }

    private void onTemplateSelected(String templateName) {
        homeViewModel.getState().setSelectedTemplateName(templateName);
        updateButtons();
    }

    private void onStartWorkout() {
        String selected = homeViewModel.getState().getSelectedTemplateName();
        if (selected != null) {
            startWorkoutController.execute(selected);
        }
    }

    private void onImport() {
        JFileChooser chooser = createJsonFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            importTemplateController.execute(chooser.getSelectedFile().getPath());
        }
    }

    private void onExport() {
        String selected = homeViewModel.getState().getSelectedTemplateName();
        if (selected == null) {
            return;
        }
        JFileChooser chooser = createJsonFileChooser();
        chooser.setSelectedFile(new File(selected + "." + JSON_EXTENSION));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getPath();
            if (!path.toLowerCase().endsWith("." + JSON_EXTENSION)) {
                path += "." + JSON_EXTENSION;
            }
            exportTemplateController.execute(selected, path);
        }
    }

    private JFileChooser createJsonFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("JSON files", JSON_EXTENSION));
        return chooser;
    }

    private void updateView(HomeState state) {
        updatingList = true;
        try {
            List<String> names = state.getTemplateNames();
            templateListModel.clear();
            for (String name : names) {
                templateListModel.addElement(name);
            }
            String selected = state.getSelectedTemplateName();
            if (selected != null && names.contains(selected)) {
                templateList.setSelectedValue(selected, true);
            } else {
                templateList.clearSelection();
                state.setSelectedTemplateName(null);
            }
        } finally {
            updatingList = false;
        }

        if (state.getError() != null) {
            statusLabel.setForeground(ERROR_COLOR);
            statusLabel.setText(state.getError());
        } else if (state.getMessage() != null) {
            statusLabel.setForeground(getForeground());
            statusLabel.setText(state.getMessage());
        } else {
            statusLabel.setText(" ");
        }

        updateButtons();
    }

    /** Start Workout and Export are only enabled while a template is selected. */
    private void updateButtons() {
        boolean hasSelection = homeViewModel.getState().getSelectedTemplateName() != null;
        startWorkoutButton.setEnabled(hasSelection);
        exportButton.setEnabled(hasSelection);
    }

    /** @param controller the controller for the load templates use case */
    public void setLoadTemplatesController(LoadTemplatesController controller) {
        this.loadTemplatesController = controller;
    }

    /** @param controller the controller for the start workout use case */
    public void setStartWorkoutController(StartWorkoutController controller) {
        this.startWorkoutController = controller;
    }

    /** @param controller the controller for the view history use case */
    public void setViewHistoryController(ViewHistoryController controller) {
        this.viewHistoryController = controller;
    }

    /** @param controller the controller for the import template use case */
    public void setImportTemplateController(ImportTemplateController controller) {
        this.importTemplateController = controller;
    }

    /** @param controller the controller for the export template use case */
    public void setExportTemplateController(ExportTemplateController controller) {
        this.exportTemplateController = controller;
    }
}
