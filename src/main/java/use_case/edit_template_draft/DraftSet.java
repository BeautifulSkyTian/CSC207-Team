package use_case.edit_template_draft;

import java.util.Objects;

/** Immutable raw set input. Blank and nonnumeric text is retained until saving. */
public final class DraftSet {
    private final String weightText;
    private final String repsText;
    private final String timeText;
    private final String distanceText;

    public DraftSet(String weightText, String repsText, String timeText, String distanceText) {
        this.weightText = Objects.requireNonNull(weightText, "weightText");
        this.repsText = Objects.requireNonNull(repsText, "repsText");
        this.timeText = Objects.requireNonNull(timeText, "timeText");
        this.distanceText = Objects.requireNonNull(distanceText, "distanceText");
    }

    public String getWeightText() {
        return weightText;
    }

    public String getRepsText() {
        return repsText;
    }

    public String getTimeText() {
        return timeText;
    }

    public String getDistanceText() {
        return distanceText;
    }
}
