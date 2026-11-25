
package entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an ongoing workout session created from a template.
 *
 * Maintains independent copies of the template's exercise
 * groups and tracks which sets have been completed.
 */
public class OngoingWorkout {

    private final String sessionId;
    private final String templateId;
    private final String templateName;
    private final LocalDateTime startTime;

    private final List<SessionExerciseGroup> groups;

    private boolean finished;

    /**
     * Creates a new workout session from a template.
     *
     * @param sessionId unique identifier for this session
     * @param template template used to start the workout
     */
    public OngoingWorkout(String sessionId, Template template) {
        this(sessionId, template, LocalDateTime.now());
    }

    /**
     * Constructor allowing an explicit start time.
     * Useful for tests.
     */
    public OngoingWorkout(String sessionId,
                          Template template,
                          LocalDateTime startTime) {

        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Session ID cannot be empty");
        }

        if (template == null) {
            throw new IllegalArgumentException(
                    "Template cannot be null");
        }

        if (startTime == null) {
            throw new IllegalArgumentException(
                    "Start time cannot be null");
        }

        this.sessionId = sessionId;
        this.templateId = template.getTemplateId();
        this.templateName = template.getName();
        this.startTime = startTime;

        this.groups = new ArrayList<>();

        for (ExerciseSetGroup group : template.getGroups()) {
            groups.add(new SessionExerciseGroup(group));
        }

        this.finished = false;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public int getGroupCount() {
        return groups.size();
    }

    /**
     * Returns a copy of the specified session group.
     *
     * This prevents callers from bypassing the workout's
     * rules by modifying internal groups directly.
     */
    public SessionExerciseGroup getGroup(int groupIndex) {
        return new SessionExerciseGroup(groups.get(groupIndex));
    }

    /**
     * Updates the completion status of a specific set.
     *
     * @param groupIndex zero-based exercise group index
     * @param setIndex zero-based set index
     * @param completed whether the set was completed
     */
    public void setCompleted(int groupIndex,
                             int setIndex,
                             boolean completed) {

        if (finished) {
            throw new IllegalStateException(
                    "Cannot modify a finished workout");
        }

        groups.get(groupIndex)
                .setCompleted(setIndex, completed);
    }

    /**
     * Checks whether a particular set has been completed.
     */
    public boolean isCompleted(int groupIndex, int setIndex) {
        return groups.get(groupIndex).isCompleted(setIndex);
    }

    /**
     * Returns the total number of completed sets
     * across all exercise groups.
     */
    public int getCompletedSetCount() {
        int count = 0;

        for (SessionExerciseGroup group : groups) {
            count += group.getCompletedSetCount();
        }

        return count;
    }

    /**
     * Returns log records containing only completed sets.
     *
     * Exercise groups without any completed sets
     * are omitted from the result.
     */
    public List<LogExerciseSet> getCompletedExercises() {
        List<LogExerciseSet> result = new ArrayList<>();

        for (SessionExerciseGroup group : groups) {
            LogExerciseSet completed =
                    group.getCompletedLogExercise();

            if (completed != null) {
                result.add(completed);
            }
        }

        return result;
    }

    public boolean isFinished() {
        return finished;
    }

    /**
     * Marks this session as finished.
     * A finished session cannot be modified.
     */
    public void markFinished() {
        if (finished) {
            throw new IllegalStateException(
                    "Workout is already finished");
        }

        finished = true;
    }
}
