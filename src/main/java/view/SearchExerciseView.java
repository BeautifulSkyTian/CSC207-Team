package view;

import entities.Exercise;
import interface_adapter.search_exercise.SearchExerciseController;
import interface_adapter.search_exercise.SearchExerciseViewModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

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

  /**
   * Constructs a {@code SearchExerciseView} and binds it to the given controller and view model.
   *
   * @param controller the controller handling search and detail requests
   * @param viewModel the observable view model storing displayed and selected exercises
   * @param bodyPartOptions initial options for the body part filter dropdown
   * @param targetMuscleOptions initial options for the target muscle filter dropdown
   * @param equipmentOptions initial options for the equipment filter dropdown
   */
  public SearchExerciseView(
      SearchExerciseController controller,
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

  /**
   * Builds a dropdown option array prepended with {@value #ALL_OPTION}.
   *
   * @param options raw option values from data/setup
   * @return non-empty array of dropdown items starting with {@value #ALL_OPTION}
   */
  private String[] buildOptionsArray(List<String> options) {
    if (options == null || options.isEmpty()) {
      return new String[] {ALL_OPTION};
    }
    String[] arr = new String[options.size() + 1];
    arr[0] = ALL_OPTION;
    for (int i = 0; i < options.size(); i++) {
      arr[i + 1] = options.get(i);
    }
    return arr;
  }

  /**
   * Initializes the Swing layout for filter controls, results list, and status bar.
   */
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

  /**
   * Registers action and mouse listeners for search triggers and row selection.
   */
  private void initListeners() {
    searchButton.addActionListener(e -> triggerSearch());
    nameSearchField.addActionListener(e -> triggerSearch());

    // Single click on a row requests full exercise details asynchronously via the controller
    resultsList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() >= 1 && SwingUtilities.isLeftMouseButton(e)) {
          int index = resultsList.locationToIndex(e.getPoint());
          Rectangle cellBounds = (index >= 0)
              ? resultsList.getCellBounds(index, index)
              : null;

          if (cellBounds != null && cellBounds.contains(e.getPoint())) {
            resultsList.setSelectedIndex(index);
            Exercise clicked = resultsListModel.getElementAt(index);
            if (clicked != null && clicked.getId() != null) {
              requestExerciseDetails(clicked.getId());
            }
          } else {
            resultsList.clearSelection();
          }
        }
      }
    });
  }

  /**
   * Normalizes current filter inputs and runs a search off the Swing Event Dispatch Thread.
   */
  private void triggerSearch() {
    String nameQuery = normalizeFilterValue(nameSearchField.getText());
    String bodyPart = normalizeFilterValue((String) bodyPartComboBox.getSelectedItem());
    String targetMuscle = normalizeFilterValue((String) targetMuscleComboBox.getSelectedItem());
    String equipment = normalizeFilterValue((String) equipmentComboBox.getSelectedItem());

    setSearchControlsEnabled(false);
    statusLabel.setText("Searching...");

    new SwingWorker<Void, Void>() {
      @Override
      protected Void doInBackground() {
        controller.search(nameQuery, bodyPart, targetMuscle, equipment);
        return null;
      }

      @Override
      protected void done() {
        setSearchControlsEnabled(true);
        try {
          get(); // surfaces any unexpected exception thrown in doInBackground
        } catch (Exception ex) {
          statusLabel.setText("Error: " + ex.getMessage());
        }
      }
    }.execute();
  }

  /**
   * Enables or disables search inputs and button while a search request is running.
   *
   * @param enabled {@code true} to enable search controls, {@code false} to disable them
   */
  private void setSearchControlsEnabled(boolean enabled) {
    searchButton.setEnabled(enabled);
    nameSearchField.setEnabled(enabled);
    bodyPartComboBox.setEnabled(enabled);
    targetMuscleComboBox.setEnabled(enabled);
    equipmentComboBox.setEnabled(enabled);
  }

  /**
   * Converts blank or {@value #ALL_OPTION} filter selections to {@code null}.
   *
   * @param value raw filter string from a text field or combo box
   * @return trimmed filter string, or {@code null} when unfiltered
   */
  private String normalizeFilterValue(String value) {
    if (value == null || value.isBlank() || ALL_OPTION.equalsIgnoreCase(value.trim())) {
      return null;
    }
    return value.trim();
  }

  /**
   * Handles view model property updates on the Swing Event Dispatch Thread.
   *
   * @param evt the property change event emitted by {@link SearchExerciseViewModel}
   */
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

  /**
   * Replaces current list rows with the provided search results and updates the status label.
   *
   * @param exercises exercises to display in the list, or {@code null}/empty to clear
   */
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

  /**
   * Opens a modal detail dialog showing exercise metadata, preview GIF, and instructions.
   *
   * @param exercise the selected exercise whose details should be displayed
   */
  private void showExerciseDetailDialog(Exercise exercise) {
    if (exercise == null) {
      return;
    }
    statusLabel.setText(" ");

    Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
    JDialog dialog = new JDialog(owner, exercise.getName(), true);
    dialog.setLayout(new BorderLayout(10, 10));
    dialog.setSize(500, 560);
    dialog.setLocationRelativeTo(this);

    JPanel content = new JPanel(new BorderLayout(8, 8));
    content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

    // Top section: Title + animated GIF preview
    JPanel headerPanel = new JPanel();
    headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

    String heading = exercise.getName() != null ? exercise.getName() : "Exercise Details";
    JLabel titleLabel = new JLabel(heading);
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
    titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    headerPanel.add(titleLabel);

    JLabel gifLabel = new JLabel();
    gifLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    gifLabel.setHorizontalAlignment(SwingConstants.CENTER);
    loadExerciseGif(exercise.getVideoUrl(), gifLabel, dialog);
    headerPanel.add(Box.createVerticalStrut(8));
    headerPanel.add(gifLabel);

    content.add(headerPanel, BorderLayout.NORTH);

    // Center section: Metadata + Instructions
    JTextArea detailsArea = new JTextArea();
    detailsArea.setEditable(false);
    detailsArea.setLineWrap(true);
    detailsArea.setWrapStyleWord(true);
    detailsArea.setText(formatExerciseDetails(exercise));
    detailsArea.setCaretPosition(0);

    JScrollPane detailScroll = new JScrollPane(detailsArea);
    content.add(detailScroll, BorderLayout.CENTER);

    // Bottom section: Close button
    JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(e -> dialog.dispose());
    bottomBar.add(closeButton);
    content.add(bottomBar, BorderLayout.SOUTH);

    dialog.add(content);
    dialog.setVisible(true);
  }

  /**
   * Formats exercise attributes, media URL, and step-by-step instructions into display text.
   *
   * @param exercise the exercise to format
   * @return formatted multi-line summary text
   */
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

  /**
   * Joins a non-empty string list with commas or returns {@code "None"} when empty/null.
   *
   * @param items list of strings to join
   * @return comma-separated string or {@code "None"}
   */
  private String joinOrNone(List<String> items) {
    if (items == null || items.isEmpty()) {
      return "None";
    }
    return String.join(", ", items);
  }

  /**
   * Displays an error message in the bottom status label when non-blank.
   *
   * @param errorMessage error text to show in the status bar
   */
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
    /**
     * Renders an {@link Exercise} item as a two-line HTML summary row.
     *
     * @param list the parent list component
     * @param value the item being rendered
     * @param index the cell index
     * @param isSelected whether the cell is selected
     * @param cellHasFocus whether the cell currently has focus
     * @return the configured renderer component
     */
    @Override
    public Component getListCellRendererComponent(
        JList<?> list,
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

    /**
     * Escapes basic HTML characters so exercise fields render safely inside HTML labels.
     *
     * @param text raw string to escape
     * @return HTML-escaped string
     */
    private String escapeHtml(String text) {
      if (text == null) {
        return "";
      }
      return text.replace("&", "&amp;")
          .replace("<", "&lt;")
          .replace(">", "&gt;");
    }
  }

  /**
   * Fetches and optionally scales an exercise preview GIF off the EDT, then updates
   * {@code gifLabel}.
   *
   * @param videoUrl URL of the preview GIF image
   * @param gifLabel label receiving the loaded icon or fallback status message
   * @param dialog parent dialog revalidated once the image is applied
   */
  private void loadExerciseGif(String videoUrl, JLabel gifLabel, JDialog dialog) {
    if (videoUrl == null || videoUrl.isBlank()) {
      gifLabel.setText("No preview GIF available.");
      return;
    }
    gifLabel.setText("Loading preview GIF...");

    new SwingWorker<ImageIcon, Void>() {
      @Override
      protected ImageIcon doInBackground() throws Exception {
        ImageIcon icon = new ImageIcon(URI.create(videoUrl).toURL());
        if (icon.getIconWidth() <= 0) {
          return null;
        }
        int maxWidth = 220;
        if (icon.getIconWidth() > maxWidth) {
          int scaledHeight = (int) ((double) icon.getIconHeight() * maxWidth
              / icon.getIconWidth());
          Image scaled = icon.getImage().getScaledInstance(
              maxWidth, scaledHeight, Image.SCALE_DEFAULT);
          return new ImageIcon(scaled);
        }
        return icon;
      }

      @Override
      protected void done() {
        try {
          ImageIcon loaded = get();
          if (loaded != null) {
            gifLabel.setText(null);
            gifLabel.setIcon(loaded);
            dialog.revalidate();
          } else {
            gifLabel.setText("Preview GIF unavailable.");
          }
        } catch (Exception ex) {
          gifLabel.setText("Preview GIF unavailable.");
        }
      }
    }.execute();
  }

  /**
   * Fetches full details for the selected exercise on a background thread via {@link SwingWorker}.
   *
   * @param exerciseId identifier of the exercise selected by the user
   */
  private void requestExerciseDetails(String exerciseId) {
    statusLabel.setText("Loading exercise details...");

    new SwingWorker<Void, Void>() {
      @Override
      protected Void doInBackground() {
        controller.selectExercise(exerciseId);
        return null;
      }

      @Override
      protected void done() {
        try {
          get();
        } catch (Exception ex) {
          statusLabel.setText("Error: " + ex.getMessage());
        }
      }
    }.execute();
  }
}