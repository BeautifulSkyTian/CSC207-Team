package view;

import entities.Exercise;
import entities.MeasurementType;
import interface_adapter.create_template.TemplateEditorState;
import interface_adapter.create_template.TemplateEditorViewModel;
import interface_adapter.edit_template_draft.EditTemplateDraftController;
import interface_adapter.save_template.SaveTemplateController;
import use_case.edit_template_draft.DraftExerciseGroup;
import use_case.edit_template_draft.DraftSet;
import use_case.edit_template_draft.WorkoutTemplateDraft;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/** Shared editor screen for a new template or an existing template. */
public class CreateTemplateView extends JPanel {
    private static final Color ERROR_COLOR = new Color(176, 0, 32);

    private final TemplateEditorViewModel viewModel;
    private final EditTemplateDraftController editController;
    private final ExercisePicker exercisePicker;

    private final JLabel title = new JLabel();
    private final JTextField templateName = new JTextField();
    private final JPanel exercises = new JPanel();
    private final JScrollPane exerciseScroll;
    private final JTextArea status = new JTextArea(2, 1);
    private boolean updatingText;
    private WorkoutTemplateDraft displayedDraft;

    /** Creates the editor and listens for draft updates and save feedback. */
    public CreateTemplateView(TemplateEditorViewModel viewModel, EditTemplateDraftController editController,
                              SaveTemplateController saveController, Supplier<List<Exercise>> loadCatalog,
                              Runnable cancel) {
        this.viewModel = viewModel;
        this.editController = editController;
        this.exercisePicker = new ExercisePicker(loadCatalog);

        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(createHeader(), BorderLayout.NORTH);

        exercises.setLayout(new BoxLayout(exercises, BoxLayout.Y_AXIS));
        JPanel scrollContent = new JPanel(new BorderLayout());
        scrollContent.add(exercises, BorderLayout.NORTH);
        exerciseScroll = new JScrollPane(scrollContent);
        exerciseScroll.setPreferredSize(new Dimension(420, 350));
        add(exerciseScroll, BorderLayout.CENTER);
        add(createFooter(saveController, cancel), BorderLayout.SOUTH);

        viewModel.addPropertyChangeListener(event -> updateView((TemplateEditorState) event.getNewValue()));
        updateView(viewModel.getState());
    }

    /** Returns the name of the editor's card. */
    public String getViewName() {
        return viewModel.getViewName();
    }

    private JPanel createHeader() {
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        JLabel nameLabel = new JLabel("Template name");
        nameLabel.setLabelFor(templateName);
        JPanel nameRow = new JPanel(new BorderLayout(8, 0));
        nameRow.add(nameLabel, BorderLayout.WEST);
        nameRow.add(templateName, BorderLayout.CENTER);
        templateName.getDocument().addDocumentListener((TextChangeListener)
                () -> updateText(() -> editController.rename(templateName.getText())));

        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.add(title, BorderLayout.NORTH);
        header.add(nameRow, BorderLayout.CENTER);
        return header;
    }

    private JPanel createFooter(SaveTemplateController saveController, Runnable cancel) {
        JButton addExercise = new JButton("Add Exercise");
        addExercise.addActionListener(event -> addExercise());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(event -> cancel.run());
        JButton save = new JButton("Save Template");
        save.addActionListener(event -> saveController.execute());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(addExercise);
        buttons.add(cancelButton);
        buttons.add(save);
        status.setEditable(false);
        status.setFocusable(false);
        status.setOpaque(false);
        status.setForeground(ERROR_COLOR);
        status.setLineWrap(true);
        status.setWrapStyleWord(true);
        status.getAccessibleContext().setAccessibleName("Template editor status");

        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.add(status, BorderLayout.CENTER);
        footer.add(buttons, BorderLayout.SOUTH);
        return footer;
    }

    private void updateView(TemplateEditorState state) {
        status.setText(state.getError());
        WorkoutTemplateDraft draft = state.getDraft();
        WorkoutTemplateDraft previous = displayedDraft;
        displayedDraft = draft;
        // Text fields already show what the user typed. Rebuilding them during typing would drop focus.
        if (updatingText || previous == draft) {
            return;
        }

        title.setText(draft.getTemplateId() == null ? "New Template" : "Edit Template");
        if (!templateName.getText().equals(draft.getTemplateName())) {
            updateText(() -> templateName.setText(draft.getTemplateName()));
        }

        exercises.removeAll();
        for (DraftExerciseGroup group : draft.getGroups()) {
            exercises.add(createExercisePanel(group));
            exercises.add(Box.createVerticalStrut(8));
        }
        if (draft.getGroups().isEmpty()) {
            JLabel empty = new JLabel("Add an exercise to start this template.");
            empty.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            exercises.add(empty);
        }
        exercises.revalidate();
        exercises.repaint();
    }

    private JPanel createExercisePanel(DraftExerciseGroup group) {
        UUID rowId = group.getRowId();
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(group.getExercise().getName()));

        JComboBox<MeasurementType> measurement = new JComboBox<>(MeasurementType.values());
        DefaultListCellRenderer renderer = new DefaultListCellRenderer();
        measurement.setRenderer((list, value, index, selected, focused) ->
                renderer.getListCellRendererComponent(list,
                        value == MeasurementType.WEIGHT_REPS ? "Weight and reps" : "Duration and distance",
                        index, selected, focused));
        measurement.setSelectedItem(group.getMeasurementType());
        measurement.getAccessibleContext().setAccessibleName("Measurement type");
        measurement.addActionListener(event -> {
            MeasurementType type = (MeasurementType) measurement.getSelectedItem();
            editController.changeMeasurementType(rowId, type);
        });
        JButton removeExercise = new JButton("Remove Exercise");
        removeExercise.addActionListener(event -> editController.removeExercise(rowId));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(measurement);
        controls.add(removeExercise);
        panel.add(controls);

        for (int index = 0; index < group.getSetCount(); index++) {
            panel.add(createSetRow(group, index));
        }
        JButton addSet = new JButton("Add Set");
        addSet.addActionListener(event -> editController.addSet(rowId));
        JPanel setActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        setActions.add(addSet);
        panel.add(setActions);
        return panel;
    }

    private JPanel createSetRow(DraftExerciseGroup group, int index) {
        DraftSet set = group.getSets().get(index);
        boolean weighted = group.getMeasurementType() == MeasurementType.WEIGHT_REPS;
        JTextField first = new JTextField(weighted ? set.getWeightText() : set.getTimeText(), 5);
        JTextField second = new JTextField(weighted ? set.getRepsText() : set.getDistanceText(), 5);
        TextChangeListener listener = () -> updateText(() -> {
            DraftSet values = weighted
                    ? new DraftSet(first.getText(), second.getText(), set.getTimeText(), set.getDistanceText())
                    : new DraftSet(set.getWeightText(), set.getRepsText(), first.getText(), second.getText());
            editController.updateSet(group.getRowId(), index, values);
        });
        first.getDocument().addDocumentListener(listener);
        second.getDocument().addDocumentListener(listener);

        JLabel firstLabel = new JLabel(weighted ? "Weight" : "Duration");
        firstLabel.setLabelFor(first);
        JLabel secondLabel = new JLabel(weighted ? "Reps" : "Distance");
        secondLabel.setLabelFor(second);
        JButton remove = new JButton("Remove");
        remove.getAccessibleContext().setAccessibleName("Remove set " + (index + 1));
        remove.addActionListener(event -> editController.removeSet(group.getRowId(), index));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.add(new JLabel("Set " + (index + 1)));
        row.add(firstLabel);
        row.add(first);
        row.add(secondLabel);
        row.add(second);
        row.add(remove);
        return row;
    }

    private void addExercise() {
        Exercise exercise = exercisePicker.choose(this);
        if (exercise == null) {
            return;
        }
        editController.addExercise(exercise, MeasurementType.WEIGHT_REPS);
        List<DraftExerciseGroup> groups = viewModel.getState().getDraft().getGroups();
        editController.addSet(groups.get(groups.size() - 1).getRowId());
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = exerciseScroll.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private void updateText(Runnable action) {
        if (updatingText) {
            return;
        }
        updatingText = true;
        try {
            action.run();
        } finally {
            updatingText = false;
        }
    }
}
