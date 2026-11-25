package view;

import entities.Exercise;
import interface_adapter.search_exercise.SearchExerciseController;
import interface_adapter.search_exercise.SearchExerciseViewModel;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

/**
 * Swing view for searching and filtering exercises, displaying a scrollable list
 * of matching exercises, and opening a popup dialog with full exercise details.
 */
public class SearchExerciseView extends JPanel implements PropertyChangeListener {
    private static final String ALL_OPTION = "All";

    private final SearchExerciseController controller;
    private final SearchExerciseViewModel viewModel;

    private final JTextField nameSearchField;
    private final JComboBox<String> bodyPartComboBox;
    private final JComboBox<String> targetMuscleComboBox;
    private final JComboBox<String> equipmentComboBox;
    private final JButton searchButton;
    private final JLabel statusLabel;

    private final DefaultListModel<Exercise> resultsListModel;
    private final JList<Exercise> resultsList;

    public SearchExerciseView(SearchExerciseController controller,
                              SearchExerciseViewModel viewModel,
                              List<String> bodyPartOptions,
                              List<String> targetMuscleOptions,
                              List<String> equipmentOptions) {
        this.controller = Objects.requireNonNull(controller, "Controller must not be null");
        this.viewModel = Objects.requireNonNull(viewModel, "ViewModel must not be null");
        this.viewModel.addPropertyChangeListener(this);

        this.nameSearchField = new JTextField(16);
        this.bodyPartComboBox = new JComboBox<>(buildOptionsArray(bodyPartOptions));
        this.targetMuscleComboBox = new JComboBox<>(buildOptionsArray(targetMuscleOptions));
        this.equipmentComboBox = new JComboBox<>(buildOptionsArray(equipmentOptions));
        this.searchButton = new JButton("Search");
        this.statusLabel = new JLabel(" ");

        this.resultsListModel = new DefaultListModel<>();
        this.resultsList = new JList<>(resultsListModel);

        initLayout();
        initListeners();
    }

    private String[] buildOptionsArray(List<String> options) {
        if (options == null || options.isEmpty()) {
            return new String[]{ALL_OPTION};
        }
        String[] arr = new String[options.size() + 1];
        arr[0] = ALL_OPTION;
        for (int i = 0; i < options.size(); i++) {
            arr[i + 1] = options.get(i);
        }
        return arr;
    }

    private void initLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Top panel: Name input + Dropdown filters + Search button
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Search & Filter Exercises"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        filterPanel.add(new JLabel("Name:"), gbc);

        gbc.gridx = 1;
        filterPanel.add(nameSearchField, gbc);

        gbc.gridx = 2;
        filterPanel.add(new JLabel("Body Part:"), gbc);

        gbc.gridx = 3;
        filterPanel.add(bodyPartComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        filterPanel.add(new JLabel("Target Muscle:"), gbc);

        gbc.gridx = 1;
        filterPanel.add(targetMuscleComboBox, gbc);

        gbc.gridx = 2;
        filterPanel.add(new JLabel("Equipment:"), gbc);

        gbc.gridx = 3;
        filterPanel.add(equipmentComboBox, gbc);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionRow.add(searchButton);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        filterPanel.add(actionRow, gbc);

        add(filterPanel, BorderLayout.NORTH);

        // Center panel: Scrollable list of clickable exercise rows
        resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsList.setFixedCellHeight(54);
        resultsList.setCellRenderer(new ExerciseRowRenderer());

        JScrollPane scrollPane = new JScrollPane(resultsList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Exercises"));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel: Status / error feedback
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 2, 0, 2));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void initListeners() {
        searchButton.addActionListener(e -> triggerSearch());
        nameSearchField.addActionListener(e -> triggerSearch());

        // Single click on a row requests full exercise details via the controller
        resultsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 1 && SwingUtilities.isLeftMouseButton(e)) {
                    int index = resultsList.locationToIndex(e.getPoint());
                    Rectangle cellBounds = (index >= 0) ? resultsList.getCellBounds(index, index) : null;

                    if (cellBounds != null && cellBounds.contains(e.getPoint())) {
                        resultsList.setSelectedIndex(index);
                        Exercise clicked = resultsListModel.getElementAt(index);
                        if (clicked != null && clicked.getId() != null) {
                            statusLabel.setText("Loading exercise details...");
                            controller.selectExercise(clicked.getId());
                        }
                    } else {
                        resultsList.clearSelection();
                    }
                }
            }
        });
    }

    private void triggerSearch() {
        String nameQuery = normalizeFilterValue(nameSearchField.getText());
        String bodyPart = normalizeFilterValue((String) bodyPartComboBox.getSelectedItem());
        String targetMuscle = normalizeFilterValue((String) targetMuscleComboBox.getSelectedItem());
        String equipment = normalizeFilterValue((String) equipmentComboBox.getSelectedItem());

        statusLabel.setText("Searching...");
        controller.search(nameQuery, bodyPart, targetMuscle, equipment);
    }

    private String normalizeFilterValue(String value) {
        if (value == null || value.isBlank() || ALL_OPTION.equalsIgnoreCase(value.trim())) {
            return null;
        }
        return value.trim();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        SwingUtilities.invokeLater(() -> {
            if (SearchExerciseViewModel.DISPLAYED_EXERCISES_PROPERTY.equals(propertyName)) {
                renderResults(viewModel.getDisplayedExercises());
            } else if (SearchExerciseViewModel.SELECTED_EXERCISE_PROPERTY.equals(propertyName)) {
                showExerciseDetailDialog(viewModel.getSelectedExercise());
            } else if (SearchExerciseViewModel.ERROR_MESSAGE_PROPERTY.equals(propertyName)) {
                renderError(viewModel.getErrorMessage());
            }
        });
    }

    private void renderResults(List<Exercise> exercises) {
        resultsListModel.clear();
        if (exercises == null || exercises.isEmpty()) {
            statusLabel.setText("No matching exercises found.");
            return;
        }
        for (Exercise exercise : exercises) {
            resultsListModel.addElement(exercise);
        }
        statusLabel.setText(exercises.size() + " exercise(s) found. Click a row for details.");
    }

    private void showExerciseDetailDialog(Exercise exercise) {
        if (exercise == null) {
            return;
        }
        statusLabel.setText(" ");

        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, exercise.getName(), true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(480, 400);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel titleLabel = new JLabel(exercise.getName() != null ? exercise.getName() : "Exercise Details");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        content.add(titleLabel, BorderLayout.NORTH);

        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setText(formatExerciseDetails(exercise));
        detailsArea.setCaretPosition(0);

        JScrollPane detailScroll = new JScrollPane(detailsArea);
        content.add(detailScroll, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        bottomBar.add(closeButton);
        content.add(bottomBar, BorderLayout.SOUTH);

        dialog.add(content);
        dialog.setVisible(true);
    }

    private String formatExerciseDetails(Exercise exercise) {
        StringBuilder sb = new StringBuilder();
        sb.append("Body Parts: ").append(joinOrNone(exercise.getBodyParts())).append("\n");
        sb.append("Target Muscles: ").append(joinOrNone(exercise.getTargetMuscles())).append("\n");
        sb.append("Equipment: ").append(joinOrNone(exercise.getEquipments())).append("\n");
        sb.append("Video URL: ").append(
                (exercise.getVideoUrl() == null || exercise.getVideoUrl().isBlank())
                        ? "N/A"
                        : exercise.getVideoUrl()
        ).append("\n\n");

        sb.append("Instructions:\n");
        List<String> instructions = exercise.getInstructions();
        if (instructions == null || instructions.isEmpty()) {
            sb.append("No instructions available.");
        } else {
            for (int i = 0; i < instructions.size(); i++) {
                sb.append(i + 1).append(". ").append(instructions.get(i)).append("\n");
            }
        }
        return sb.toString();
    }

    private String joinOrNone(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "None";
        }
        return String.join(", ", items);
    }

    private void renderError(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return;
        }
        statusLabel.setText("Error: " + errorMessage);
    }

    /**
     * Custom cell renderer formatting each row with the exercise name and summary tags.
     */
    private static class ExerciseRowRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setPreferredSize(new Dimension(0, 54));
            if (value instanceof Exercise) {
                Exercise ex = (Exercise) value;
                String muscles = ex.getTargetMuscles().isEmpty()
                        ? "General"
                        : String.join(", ", ex.getTargetMuscles());
                String equip = ex.getEquipments().isEmpty()
                        ? "Bodyweight / None"
                        : String.join(", ", ex.getEquipments());
                setText("<html><b>" + escapeHtml(ex.getName()) + "</b><br/>"
                        + "<span style='color:#555555;font-size:11px;'>"
                        + "Target: " + escapeHtml(muscles) + " | Equipment: " + escapeHtml(equip)
                        + "</span></html>");
            }
            return this;
        }

        private String escapeHtml(String text) {
            if (text == null) {
                return "";
            }
            return text.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;");
        }
    }
}