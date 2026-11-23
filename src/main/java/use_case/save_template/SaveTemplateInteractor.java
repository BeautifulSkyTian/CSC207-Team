package use_case.save_template;

import entities.ExerciseSetGroup;
import entities.MeasurementType;
import entities.Template;
import use_case.TemplateDataAccessInterface;
import use_case.edit_template_draft.DraftExerciseGroup;
import use_case.edit_template_draft.DraftSet;
import use_case.edit_template_draft.WorkoutTemplateDraft;

import java.io.UncheckedIOException;
import java.util.Objects;

/** Validates a draft, builds a shared Template, and reports the outcome of saving it. */
public final class SaveTemplateInteractor implements SaveTemplateInputBoundary {
    private final TemplateDataAccessInterface templateDataAccess;
    private final SaveTemplateOutputBoundary presenter;

    public SaveTemplateInteractor(TemplateDataAccessInterface templateDataAccess,
                                  SaveTemplateOutputBoundary presenter) {
        this.templateDataAccess = Objects.requireNonNull(templateDataAccess);
        this.presenter = Objects.requireNonNull(presenter);
    }

    @Override
    public void execute(SaveTemplateInputData input) {
        Template template;
        try {
            template = buildTemplate(input.getDraft());
        } catch (IllegalArgumentException e) {
            presenter.presentFailure(e.getMessage());
            return;
        }

        boolean saved;
        try {
            saved = saveIfNameAvailable(template);
        } catch (UncheckedIOException e) {
            presenter.presentFailure("Could not write the workout template file.");
            return;
        }

        if (saved) {
            presenter.presentSuccess(new SaveTemplateOutputData(template.getTemplateId(), template.getName()));
        } else {
            presenter.presentFailure("A different template already uses that name.");
        }
    }

    private boolean saveIfNameAvailable(Template template) {
        synchronized (templateDataAccess) {
            Template existing = templateDataAccess.get(template.getName());
            if (existing != null && !existing.getTemplateId().equals(template.getTemplateId())) {
                return false;
            }
            templateDataAccess.save(template);
            return true;
        }
    }

    private static Template buildTemplate(WorkoutTemplateDraft draft) {
        String name = draft.getTemplateName().strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Enter a template name.");
        }
        if (draft.getGroups().isEmpty()) {
            throw new IllegalArgumentException("Add at least one exercise before saving.");
        }

        String templateId = draft.getTemplateId();
        Template template = templateId == null ? new Template(name) : new Template(templateId, name);
        int rowNumber = 1;
        for (DraftExerciseGroup row : draft.getGroups()) {
            ExerciseSetGroup group = new ExerciseSetGroup(row.getExercise(), row.getMeasurementType());
            int setNumber = 1;
            for (DraftSet set : row.getSets()) {
                addSet(group, set, "Exercise " + rowNumber + " (" + row.getExercise().getName() + "), set "
                        + setNumber + ": ");
                setNumber++;
            }
            template.addGroup(group);
            rowNumber++;
        }
        return template;
    }

    private static void addSet(ExerciseSetGroup group, DraftSet set, String location) {
        if (group.getMeasurementType() == MeasurementType.WEIGHT_REPS) {
            double weight = parseDecimal(set.getWeightText(), location + "weight");
            int reps = parseInteger(set.getRepsText(), location + "reps");
            group.addWeightRepSet(weight, reps);
        } else {
            int time = parseInteger(set.getTimeText(), location + "time");
            double distance = parseDecimal(set.getDistanceText(), location + "distance");
            group.addTimeDistanceSet(time, distance);
        }
    }

    private static int parseInteger(String text, String field) {
        try {
            return Integer.parseInt(text.strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " must be a whole number.", e);
        }
    }

    private static double parseDecimal(String text, String field) {
        double value;
        try {
            value = Double.parseDouble(text.strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " must be a finite number.", e);
        }
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(field + " must be a finite number.");
        }
        return value;
    }
}
