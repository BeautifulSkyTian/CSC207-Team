package use_case.edit_template_draft;

import java.util.Objects;

/**
 * Immutable raw weight and rep input, matching the shared ExerciseSetGroup.
 * Blank and nonnumeric text is retained for validation by the save use case.
 */
public final class DraftSet {
    private final String weightText;
    private final String repsText;

    public DraftSet(String weightText, String repsText) {
        this.weightText = Objects.requireNonNull(weightText, "weightText");
        this.repsText = Objects.requireNonNull(repsText, "repsText");
    }

    public String getWeightText() {
        return weightText;
    }

    public String getRepsText() {
        return repsText;
    }
}
