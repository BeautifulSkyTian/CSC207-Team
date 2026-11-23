package use_case.search_exercise;
import entities.Exercise;
import java.util.List;

/**
 * Data Access Interface for the Search Exercises use case.
 * Defines the contract for retrieving the master list of exercises
 * without coupling the use case layer to external APIs or caching mechanics.
 */
public interface SearchExerciseDataAccessInterface {

    /**
     * Retrieves all available exercises from the underlying data source.
     * Returns summary Exercise objects (without VideoURL or instructions).
     *
     * @return a list of all {@link Exercise} entities
     */
    List<Exercise> fetchAllExercises();

    /**
     * Retrieves the full detailed exercise from the data source by ID.
     *
     * @param id is the specific exercise's ID
     * @return an exercise entity
     */
    Exercise getExerciseById(String id);

}

