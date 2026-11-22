package data_access;

import entities.Exercise;
import entities.ExerciseSetGroup;
import entities.Template;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonTemplateDataAccessObjectTest {

    @TempDir
    Path tempDir;

    @Test
    void templatesPersistAcrossInstances() {
        String path = tempDir.resolve("userdata/templates.json").toString();
        Exercise bench = new Exercise.ExerciseBuilder()
                .id("0025")
                .name("barbell bench press")
                .targetMuscles(List.of("pectorals"))
                .equipments(List.of("barbell"))
                .bodyParts(List.of("chest"))
                .videoUrl(null)
                .instructions(List.of("Lie down.", "Press."))
                .build();
        ExerciseSetGroup group = new ExerciseSetGroup(bench);
        group.addSet(60.0, 10);
        group.addSet(65.5, 8);
        Template push = new Template("Push Day");
        push.addGroup(group);

        new JsonTemplateDataAccessObject(path).save(push);

        JsonTemplateDataAccessObject reloaded = new JsonTemplateDataAccessObject(path);
        assertTrue(reloaded.existsByName("Push Day"));
        Template loaded = reloaded.get("Push Day");
        ExerciseSetGroup loadedGroup = loaded.getGroup(0);
        assertEquals("barbell bench press", loadedGroup.getExercise().getName());
        assertEquals(List.of("chest"), loadedGroup.getExercise().getBodyParts());
        assertNull(loadedGroup.getExercise().getVideoUrl());
        assertEquals(List.of(60.0, 65.5), loadedGroup.getWeights());
        assertEquals(List.of(10, 8), loadedGroup.getRepsList());

        assertTrue(reloaded.delete("Push Day"));
        assertFalse(reloaded.delete("Push Day"));
        assertTrue(new JsonTemplateDataAccessObject(path).getAll().isEmpty());
    }
}
