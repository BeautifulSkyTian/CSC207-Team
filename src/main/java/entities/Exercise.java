package entities;

import java.util.List;

public class Exercise {
    private final String id;
    private final String name;
    private final List<String> targetMuscles;
    private final List<String> equipments;
    private final List<String> bodyParts;
    private final String videoUrl;
    private final List<String> instructions;

    public Exercise(String id,
                    String name,
                    List<String> targetMuscles,
                    List<String> equipments,
                    List<String> bodyParts,
                    String videoUrl,
                    List<String> instructions) {
        this.id = id;
        this.name = name;
        this.videoUrl = videoUrl;

        if (targetMuscles == null) {
            this.targetMuscles = List.of();
        } else {
            this.targetMuscles = List.copyOf(targetMuscles);
        }

        if (equipments == null) {
            this.equipments = List.of();
        } else {
            this.equipments = List.copyOf(equipments);
        }

        if (bodyParts == null) {
            this.bodyParts = List.of();
        } else {
            this.bodyParts = List.copyOf(bodyParts);
        }

        if (instructions == null) {
            this.instructions = List.of();
        } else {
            this.instructions = List.copyOf(instructions);
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getTargetMuscles() {
        return targetMuscles;
    }

    public List<String> getEquipments() {
        return equipments;
    }

    public List<String> getBodyParts() {
        return bodyParts;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public List<String> getInstructions() {
        return instructions;
    }
}