package entities;

/**
 * Criteria for searching exercises: by name, body part, target muscle and/or equipment.
 * A null/empty criterion means "don't filter on this field".
 */
public class FilterCriteria {
  private final String nameQuery;
  private final String bodyPart;
  private final String targetMuscle;
  private final String equipment;

  /**
   * Creates filter criteria for matching exercises.
   *
   * @param nameQuery partial exercise name to match
   * @param bodyPart body part filter
   * @param targetMuscle target muscle filter
   * @param equipment equipment filter
   */
  public FilterCriteria(String nameQuery, String bodyPart, String targetMuscle, String equipment) {
    this.nameQuery = nameQuery;
    this.bodyPart = bodyPart;
    this.targetMuscle = targetMuscle;
    this.equipment = equipment;
  }

  public String getNameQuery() {
    return nameQuery;
  }

  public String getBodyPart() {
    return bodyPart;
  }

  public String getTargetMuscle() {
    return targetMuscle;
  }

  public String getEquipment() {
    return equipment;
  }

  private static boolean isBlank(String s) {
    return s == null || s.isBlank();
  }
}