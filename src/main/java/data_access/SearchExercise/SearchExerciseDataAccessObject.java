package data_access.SearchExercise;

import entities.Exercise;
import java.util.List;
import use_case.search_exercise.SearchExerciseDataAccessInterface;

/**
 * Direct API implementation of {@link SearchExerciseDataAccessInterface}.
 */
public class SearchExerciseDataAccessObject implements SearchExerciseDataAccessInterface {

    private final ExternalExerciseApiGateway apiGateway;

    /**
     * Constructs the DAO with an {@link ExternalExerciseApiGateway}.
     *
     * @param apiGateway the gateway used to fetch exercise data from the API
     */
    public SearchExerciseDataAccessObject(ExternalExerciseApiGateway apiGateway) {
        this.apiGateway = apiGateway;
    }

    @Override
    public List<Exercise> fetchAllExercises() {
        return apiGateway.fetchAllExercises();
    }

    @Override
    public Exercise getExerciseById(String id) {
        return apiGateway.fetchExerciseById(id);
    }
}