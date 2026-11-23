package use_case.search_exercise;

import entities.Exercise;
import entities.FilterCriteria;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Interactor for the Search Exercises use case.
 * Coordinates fetching summary exercises, filtering them against {@link FilterCriteria},
 * and lazily retrieving full exercise details (video URL and instructions) when selected.
 */
public class SearchExerciseInteractor implements SearchExerciseInputBoundary {
    private final SearchExerciseDataAccessInterface dataAccess;
    private final SearchExerciseOutputBoundary presenter;

    /**
     * Constructs a new {@code SearchExercisesInteractor}.
     *
     * @param dataAccess the data access gateway/repository for retrieving exercises
     * @param presenter  the output boundary for presenting results or errors
     */
    public SearchExerciseInteractor(SearchExerciseDataAccessInterface dataAccess,
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
            List<Exercise> allExercises = dataAccess.fetchAllExercises();

            List<Exercise> matchingExercises = allExercises.stream()
                    .filter(criteria::matches)
                    .collect(Collectors.toList());

            SearchExerciseOutputData outputData = new SearchExerciseOutputData(matchingExercises);
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
}
