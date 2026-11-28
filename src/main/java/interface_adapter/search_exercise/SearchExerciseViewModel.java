package interface_adapter.search_exercise;

import entities.Exercise;
import entities.FilterCriteria;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Objects;

/**
 * ViewModel for the Search Exercises use case.
 * Holds presentation state (active filter criteria, matching exercise summaries,
 * selected exercise full details, and error messages) and notifies observing views
 * via {@link PropertyChangeSupport}.
 */
public class SearchExerciseViewModel {
  public static final String DISPLAYED_EXERCISES_PROPERTY = "displayedExercises";
  public static final String SELECTED_EXERCISE_PROPERTY = "selectedExercise";
  public static final String ERROR_MESSAGE_PROPERTY = "errorMessage";

  private final PropertyChangeSupport support;
  private FilterCriteria currentCriteria;
  private List<Exercise> displayedExercises;
  private Exercise selectedExercise;
  private String errorMessage;

  /**
   * Constructs a new {@code SearchExerciseViewModel} with empty initial state.
   */
  public SearchExerciseViewModel() {
    this.support = new PropertyChangeSupport(this);
    this.displayedExercises = List.of();
  }

  public FilterCriteria getCurrentCriteria() {
    return currentCriteria;
  }

  public void setCurrentCriteria(FilterCriteria currentCriteria) {
    this.currentCriteria = currentCriteria;
  }

  public List<Exercise> getDisplayedExercises() {
    return displayedExercises;
  }

  /**
   * Sets the displayed exercises list, normalizing {@code null} to an empty list.
   *
   * @param displayedExercises exercises to display
   */
  public void setDisplayedExercises(List<Exercise> displayedExercises) {
    if (displayedExercises == null) {
      this.displayedExercises = List.of();
    } else {
      this.displayedExercises = List.copyOf(displayedExercises);
    }
  }

  public Exercise getSelectedExercise() {
    return selectedExercise;
  }

  public void setSelectedExercise(Exercise selectedExercise) {
    this.selectedExercise = selectedExercise;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  /**
   * Registers a {@link PropertyChangeListener} to observe state updates.
   *
   * @param listener the listener to add; must not be null
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(
        Objects.requireNonNull(listener, "Listener must not be null"));
  }

  /**
   * Removes a previously registered {@link PropertyChangeListener}.
   *
   * @param listener the listener to remove
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    support.removePropertyChangeListener(listener);
  }

  /**
   * Notifies registered listeners that a property on this ViewModel has been updated.
   *
   * @param propertyName the name of the updated property
   */
  public void firePropertyChanged(String propertyName) {
    support.firePropertyChange(propertyName, null, getCurrentValue(propertyName));
  }

  private Object getCurrentValue(String propertyName) {
    if (DISPLAYED_EXERCISES_PROPERTY.equals(propertyName)) {
      return displayedExercises;
    }
    if (SELECTED_EXERCISE_PROPERTY.equals(propertyName)) {
      return selectedExercise;
    }
    if (ERROR_MESSAGE_PROPERTY.equals(propertyName)) {
      return errorMessage;
    }
    return null;
  }
}