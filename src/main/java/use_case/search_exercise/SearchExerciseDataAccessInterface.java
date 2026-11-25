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
     * Searches exercises matching any provided active filters.
     * Blank or null parameters are omitted from the query string.
     */
    List<Exercise> searchExercises(String name, String bodyPart, String targetMuscle, String equipment);

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

    /**
     * Retrieves all available body part filter options from the underlying data source.
     *
     * @return a list of body part names
     */
    List<String> fetchAllBodyParts();

    /**
     * Retrieves all available target muscle filter options from the underlying data source.
     *
     * @return a list of target muscle names
     */
    List<String> fetchAllMuscles();

    /**
     * Retrieves all available equipment filter options from the underlying data source.
     *
     * @return a list of equipment names
     */
    List<String> fetchAllEquipments();

}

