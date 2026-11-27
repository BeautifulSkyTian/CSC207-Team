package use_case.search_exercise;

import entities.Exercise;

/**
 * Output Boundary for the Search Exercises use case.
 * Defines the contract implemented by
 * {@link interface_adapter.search_exercise.SearchExercisePresenter}
 * to update the ViewModel with search results, full exercise details, or error messages.
 */
public interface SearchExerciseOutputBoundary {

  /**
   * Prepares the view with the list of summary exercises matching the search criteria.
   *
   * @param outputData the output data containing the matching {@link Exercise} list
   */
  void prepareSuccessView(SearchExerciseOutputData outputData);

  /**
   * Prepares the view with the full details (video URL, instructions, etc.) of a selected
   * exercise.
   *
   * @param exercise the full {@link Exercise} entity
   */
  void prepareDetailView(Exercise exercise);

  /**
   * Prepares the view when a search or exercise detail lookup fails.
   *
   * @param errorMessage a human-readable description of the error
   */
  void prepareFailView(String errorMessage);
}