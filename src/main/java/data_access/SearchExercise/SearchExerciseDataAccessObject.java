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
    public List<Exercise> searchExercises(
            String name,
            String bodyPart,
            String targetMuscle,
            String equipment) {
        return apiGateway.searchExercises(name, bodyPart, targetMuscle, equipment);
    }

    @Override
    public List<Exercise> fetchAllExercises() {
        return searchExercises(null, null, null, null);
    }

    @Override
    public Exercise getExerciseById(String id) {
        return apiGateway.getExerciseById(id);
    }

    @Override
    public List<String> fetchAllBodyParts() {
        return apiGateway.fetchAllBodyParts();
    }

    @Override
    public List<String> fetchAllMuscles() {
        return apiGateway.fetchAllMuscles();
    }

    @Override
    public List<String> fetchAllEquipments() {
        return apiGateway.fetchAllEquipments();
    }
}