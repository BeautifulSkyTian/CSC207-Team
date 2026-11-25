package view;

import entities.Exercise;
import entities.FilterCriteria;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;

/** Lets the editor choose a catalog exercise or enter a custom exercise name. */
final class ExercisePicker {
    private final Supplier<List<Exercise>> loadCatalog;
    private final JPanel form = new JPanel(new BorderLayout(0, 6));
    private final JTextField name = new JTextField(25);
    private final JLabel status = new JLabel(" ");
    private final DefaultListModel<Exercise> matches = new DefaultListModel<>();
    private final JList<Exercise> results = new JList<>(matches);

    private List<Exercise> catalog = List.of();
    private boolean loading;
    private boolean loaded;

    ExercisePicker(Supplier<List<Exercise>> loadCatalog) {
        this.loadCatalog = loadCatalog;
        JPanel search = new JPanel(new BorderLayout(0, 4));
        JLabel label = new JLabel("Search or enter a custom exercise name");
        label.setLabelFor(name);
        search.add(label, BorderLayout.NORTH);
        search.add(name, BorderLayout.CENTER);
        form.add(search, BorderLayout.NORTH);
        results.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        results.setVisibleRowCount(7);
        DefaultListCellRenderer renderer = new DefaultListCellRenderer();
        results.setCellRenderer((list, exercise, index, selected, focused) ->
                renderer.getListCellRendererComponent(
                        list, exercise.getName(), index, selected, focused));
        form.add(new JScrollPane(results), BorderLayout.CENTER);
        form.add(status, BorderLayout.SOUTH);
        name.getDocument().addDocumentListener((TextChangeListener) this::filterCatalog);
    }

    Exercise choose(Component parent) {
        name.setText("");
        filterCatalog();
        if (!loaded && !loading) {
            fetchCatalog();
        }

        Object[] options = {"Add Exercise", "Cancel"};
        while (JOptionPane.showOptionDialog(parent, form, "Add Exercise", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]) == JOptionPane.OK_OPTION) {
            Exercise selected = results.getSelectedValue();
            if (selected != null) {
                return selected;
            }
            String customName = name.getText().strip();
            if (!customName.isEmpty()) {
                return new Exercise.ExerciseBuilder()
                        .id("custom-" + UUID.randomUUID())
                        .name(customName)
                        .build();
            }
            status.setText("Select an exercise or enter a name.");
        }
        return null;
    }

    private void filterCatalog() {
        FilterCriteria criteria = new FilterCriteria(name.getText().strip(), null, null, null);
        matches.clear();
        for (Exercise exercise : catalog) {
            if (criteria.matches(exercise)) {
                matches.addElement(exercise);
            }
        }
        if (loaded) {
            status.setText(matches.isEmpty() ? "No catalog matches." : "Select a match to use it.");
        }
    }

    private void fetchCatalog() {
        loading = true;
        status.setText("Loading ExerciseDB...");
        new SwingWorker<List<Exercise>, Void>() {
            @Override
            protected List<Exercise> doInBackground() {
                return loadCatalog.get();
            }

            @Override
            protected void done() {
                loading = false;
                try {
                    catalog = get();
                    loaded = true;
                    filterCatalog();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    status.setText("Catalog loading was interrupted.");
                } catch (ExecutionException e) {
                    status.setText("ExerciseDB is unavailable. You can enter a name.");
                }
            }
        }.execute();
    }
}
