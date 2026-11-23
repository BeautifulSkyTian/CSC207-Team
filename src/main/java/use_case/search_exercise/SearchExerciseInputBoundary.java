package use_case.search_exercise;

/**
 * Input Boundary for the Search Exercises use case.
 * Defines the contract implemented by {@link SearchExerciseInteractor} and invoked by
 * {@link SearchExerciseController} for both filtered searches and individual exercise selection.
 */
public interface SearchExerciseInputBoundary {

    /**
     * Executes a search for exercises matching the provided filter criteria.
     *
     * @param inputData the input data containing the {@link entities.FilterCriteria}
     */
    void executeSearch(SearchExerciseInputData inputData);

    /**
     * Retrieves and presents full details (such as video URL and instructions) for a specific exercise.
     *
     * @param exerciseId the unique identifier of the selected exercise
     */
    void selectExercise(String exerciseId);
}
