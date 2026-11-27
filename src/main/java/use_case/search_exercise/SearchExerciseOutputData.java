package use_case.search_exercise;

import entities.Exercise;
import java.util.List;
import java.util.Objects;

/**
 * Output data transferred from the Search Exercises Interactor to the Presenter.
 * Contains the unmodifiable list of summary exercises matching the user's filter criteria.
 */
public class SearchExerciseOutputData {
  private final List<Exercise> matchingExercises;

  /**
   * Constructs a new {@code SearchExerciseOutputData} instance.
   *
   * @param matchingExercises the list of exercises matching the search criteria; must not be null
   */
  public SearchExerciseOutputData(List<Exercise> matchingExercises) {
    Objects.requireNonNull(matchingExercises, "Matching exercises list must not be null");
    this.matchingExercises = List.copyOf(matchingExercises);
  }

  /**
   * Returns an unmodifiable list of exercises matching the search criteria.
   *
   * @return the matching {@link Exercise} list
   */
  public List<Exercise> getMatchingExercises() {
    return matchingExercises;
  }
}