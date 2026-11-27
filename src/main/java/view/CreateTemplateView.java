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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
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
    private final WrappingText status = new WrappingText("");
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
        exerciseScroll = new JScrollPane(new VerticalScrollPanel(exercises),
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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

        JPanel saveActions = new JPanel(new FlowLayout(FlowLayout.TRAILING, 8, 0));
        saveActions.add(cancelButton);
        saveActions.add(save);
        JPanel buttons = new JPanel(new BorderLayout(8, 0));
        buttons.add(addExercise, BorderLayout.WEST);
        buttons.add(saveActions, BorderLayout.EAST);
        status.setForeground(ERROR_COLOR);
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
            WrappingText empty = new WrappingText("Add an exercise to start this template.");
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
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        WrappingText exerciseName = new WrappingText(group.getExercise().getName());
        exerciseName.setFont(exerciseName.getFont().deriveFont(Font.BOLD));
        JPanel heading = new JPanel(new BorderLayout());
        heading.add(exerciseName, BorderLayout.CENTER);
        panel.add(heading);

        JComboBox<MeasurementType> measurement = new JComboBox<>(MeasurementType.values());
        DefaultListCellRenderer renderer = new DefaultListCellRenderer();
        measurement.setRenderer((list, value, index, selected, focused) ->
                renderer.getListCellRendererComponent(list,
                        value == MeasurementType.WEIGHT_REPS ? "Weight and reps" : "Time and distance",
                        index, selected, focused));
        measurement.setSelectedItem(group.getMeasurementType());
        measurement.addActionListener(event -> {
            MeasurementType type = (MeasurementType) measurement.getSelectedItem();
            editController.changeMeasurementType(rowId, type);
        });
        JButton removeExercise = new JButton("Remove Exercise");
        removeExercise.addActionListener(event -> editController.removeExercise(rowId));
        JPanel controls = new JPanel(new BorderLayout(8, 0));
        controls.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0));
        controls.add(labeledField("Measurements", measurement), BorderLayout.CENTER);
        JPanel removeAction = new JPanel(new BorderLayout());
        removeAction.add(removeExercise, BorderLayout.SOUTH);
        controls.add(removeAction, BorderLayout.EAST);
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

        JButton remove = new JButton("Remove");
        remove.getAccessibleContext().setAccessibleName("Remove set " + (index + 1));
        remove.addActionListener(event -> editController.removeSet(group.getRowId(), index));

        JPanel fields = new JPanel(new GridLayout(1, 2, 8, 0));
        fields.add(labeledField(weighted ? "Weight" : "Time", first));
        fields.add(labeledField(weighted ? "Reps" : "Distance", second));
        JPanel removeAction = new JPanel(new BorderLayout());
        removeAction.add(remove, BorderLayout.SOUTH);
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        row.add(new JLabel("Set " + (index + 1)), BorderLayout.WEST);
        row.add(fields, BorderLayout.CENTER);
        row.add(removeAction, BorderLayout.EAST);
        return row;
    }

    private static JPanel labeledField(String text, JComponent field) {
        JLabel label = new JLabel(text);
        label.setLabelFor(field);
        JPanel panel = new JPanel(new BorderLayout(0, 2));
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
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
