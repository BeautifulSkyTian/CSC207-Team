package use_case.search_exercise;

import entities.Exercise;
import entities.FilterCriteria;
import java.util.List;
import java.util.Objects;

/**
 * Interactor for the Search Exercises use case.
 * Coordinates querying exercises via active {@link FilterCriteria} query parameters
 * and lazily retrieving full exercise details when an item is selected.
 */
public class SearchExerciseInteractor implements SearchExerciseInputBoundary {
  private final SearchExerciseDataAccessInterface dataAccess;
  private final SearchExerciseOutputBoundary presenter;

  /**
   * Constructs a new {@code SearchExerciseInteractor}.
   *
   * @param dataAccess the data access gateway/DAO for retrieving exercises
   * @param presenter the output boundary for presenting search results, details, or errors
   */
  public SearchExerciseInteractor(
      SearchExerciseDataAccessInterface dataAccess,
      SearchExerciseOutputBoundary presenter) {
    this.dataAccess = Objects.requireNonNull(dataAccess, "Data access must not be null");
    this.presenter = Objects.requireNonNull(presenter, "Presenter must not be null");
  }

  @Override
  public void executeSearch(SearchExerciseInputData inputData) {
    if (inputData == null || inputData.getCriteria() == null) {
      presenter.prepareFailView("Search criteria cannot be null.");
      return;
    }

    try {
      FilterCriteria criteria = inputData.getCriteria();
      String name = normalizeFilter(criteria.getNameQuery());
      String bodyPart = normalizeFilter(criteria.getBodyPart());
      String targetMuscle = normalizeFilter(criteria.getTargetMuscle());
      String equipment = normalizeFilter(criteria.getEquipment());

      List<Exercise> matchingExercises = dataAccess.searchExercises(
          name, bodyPart, targetMuscle, equipment);
      List<Exercise> safeResults = (matchingExercises == null)
          ? List.of()
          : matchingExercises;

      SearchExerciseOutputData outputData = new SearchExerciseOutputData(safeResults);
      presenter.prepareSuccessView(outputData);
    } catch (RuntimeException e) {
      presenter.prepareFailView("Failed to load exercises: " + e.getMessage());
    }
  }

  @Override
  public void selectExercise(String exerciseId) {
    if (exerciseId == null || exerciseId.isBlank()) {
      presenter.prepareFailView("Exercise ID cannot be empty.");
      return;
    }

    try {
      Exercise fullExercise = dataAccess.getExerciseById(exerciseId);

      if (fullExercise == null) {
        presenter.prepareFailView("Exercise details could not be found.");
        return;
      }

      presenter.prepareDetailView(fullExercise);
    } catch (RuntimeException e) {
      presenter.prepareFailView("Failed to load exercise details: " + e.getMessage());
    }
  }

  private String normalizeFilter(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty() || "All".equalsIgnoreCase(trimmed)) {
      return null;
    }
    return trimmed;
  }
}