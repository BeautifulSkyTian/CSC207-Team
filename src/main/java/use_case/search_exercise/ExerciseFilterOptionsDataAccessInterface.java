package use_case.search_exercise;

import java.util.List;

/**
 * Contract for retrieving available filter values used to populate search dropdowns.
 */
public interface ExerciseFilterOptionsDataAccessInterface {

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