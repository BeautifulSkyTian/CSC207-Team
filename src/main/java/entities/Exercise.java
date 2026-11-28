package entities;

import java.util.List;

/**
 * Immutable exercise entity supporting summary search results and full detail records.
 * List fields are defensively copied and exposed as unmodifiable lists.
 */
public class Exercise {
  private final String id;
  private final String name;
  private final List<String> targetMuscles;
  private final List<String> equipments;
  private final List<String> bodyParts;
  private final String videoUrl;
  private final List<String> instructions;

  /**
   * Creates an immutable {@code Exercise} from {@code ExerciseBuilder}, normalizing null lists to
   * empty.
   */
  private Exercise(ExerciseBuilder builder) {
    this.id = builder.id;
    this.name = builder.name;
    this.videoUrl = builder.videoUrl;

    if (builder.targetMuscles == null) {
      this.targetMuscles = List.of();
    } else {
      this.targetMuscles = List.copyOf(builder.targetMuscles);
    }

    if (builder.equipments == null) {
      this.equipments = List.of();
    } else {
      this.equipments = List.copyOf(builder.equipments);
    }

    if (builder.bodyParts == null) {
      this.bodyParts = List.of();
    } else {
      this.bodyParts = List.copyOf(builder.bodyParts);
    }

    if (builder.instructions == null) {
      this.instructions = List.of();
    } else {
      this.instructions = List.copyOf(builder.instructions);
    }
  }

  /**
   * Gets the exercise ID.
   *
   * @return the exercise ID
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the exercise name.
   *
   * @return the exercise name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the unmodifiable list of target muscles.
   *
   * @return target muscles list
   */
  public List<String> getTargetMuscles() {
    return targetMuscles;
  }

  /**
   * Gets the unmodifiable list of equipment.
   *
   * @return equipment list
   */
  public List<String> getEquipments() {
    return equipments;
  }

  /**
   * Gets the unmodifiable list of body parts.
   *
   * @return body parts list
   */
  public List<String> getBodyParts() {
    return bodyParts;
  }

  /**
   * Gets the video or GIF URL.
   *
   * @return the video URL
   */
  public String getVideoUrl() {
    return videoUrl;
  }

  /**
   * Gets the unmodifiable list of step-by-step instructions.
   *
   * @return instructions list
   */
  public List<String> getInstructions() {
    return instructions;
  }

  /** Fluent builder for constructing {@link Exercise} instances. */
  public static class ExerciseBuilder {
    private String id;
    private String name;
    private List<String> targetMuscles;
    private List<String> equipments;
    private List<String> bodyParts;
    private String videoUrl;
    private List<String> instructions;

    /**
     * Sets the exercise ID.
     *
     * @param id the exercise ID
     * @return this builder
     */
    public ExerciseBuilder id(String id) {
      this.id = id;
      return this;
    }

    /**
     * Sets the exercise name.
     *
     * @param name the exercise name
     * @return this builder
     */
    public ExerciseBuilder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets the target muscles list.
     *
     * @param targetMuscles target muscles list
     * @return this builder
     */
    public ExerciseBuilder targetMuscles(List<String> targetMuscles) {
      this.targetMuscles = targetMuscles;
      return this;
    }

    /**
     * Sets the equipment list.
     *
     * @param equipments equipment list
     * @return this builder
     */
    public ExerciseBuilder equipments(List<String> equipments) {
      this.equipments = equipments;
      return this;
    }

    /**
     * Sets the body parts list.
     *
     * @param bodyParts body parts list
     * @return this builder
     */
    public ExerciseBuilder bodyParts(List<String> bodyParts) {
      this.bodyParts = bodyParts;
      return this;
    }

    /**
     * Sets the video or GIF URL.
     *
     * @param videoUrl the video URL
     * @return this builder
     */
    public ExerciseBuilder videoUrl(String videoUrl) {
      this.videoUrl = videoUrl;
      return this;
    }

    /**
     * Sets the step-by-step instructions list.
     *
     * @param instructions instructions list
     * @return this builder
     */
    public ExerciseBuilder instructions(List<String> instructions) {
      this.instructions = instructions;
      return this;
    }

    /** Builds and returns the immutable {@link Exercise}. */
    public Exercise build() {
      return new Exercise(this);
    }
  }
}