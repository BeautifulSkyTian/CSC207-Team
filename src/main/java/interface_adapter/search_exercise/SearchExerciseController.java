package interface_adapter.search_exercise;

import entities.FilterCriteria;
import java.util.Objects;
import use_case.search_exercise.SearchExerciseInputBoundary;
import use_case.search_exercise.SearchExerciseInputData;

/**
 * Controller for the Search Exercises use case.
 * Converts UI actions and raw filter parameters into use case inputs and invokes
 * {@link SearchExerciseInputBoundary}.
 */
public class SearchExerciseController {
  private final SearchExerciseInputBoundary interactor;

  /**
   * Constructs a new {@code SearchExerciseController}.
   *
   * @param interactor the use case input boundary; must not be null
   */
  public SearchExerciseController(SearchExerciseInputBoundary interactor) {
    this.interactor = Objects.requireNonNull(interactor, "Input boundary must not be null");
  }

  /**
   * Initiates a search using optional filter parameters.
   * Any null or blank parameter is treated as "no filter" by {@link FilterCriteria}.
   *
   * @param nameQuery partial exercise name to match
   * @param bodyPart body part filter
   * @param targetMuscle target muscle filter
   * @param equipment equipment filter
   */
  public void search(String nameQuery, String bodyPart, String targetMuscle, String equipment) {
    FilterCriteria criteria = new FilterCriteria(nameQuery, bodyPart, targetMuscle, equipment);
    SearchExerciseInputData inputData = new SearchExerciseInputData(criteria);
    interactor.executeSearch(inputData);
  }

  /**
   * Requests full details (video URL, instructions, etc.) for a selected exercise.
   *
   * @param exerciseId the unique identifier of the clicked exercise
   */
  public void selectExercise(String exerciseId) {
    interactor.selectExercise(exerciseId);
  }
}