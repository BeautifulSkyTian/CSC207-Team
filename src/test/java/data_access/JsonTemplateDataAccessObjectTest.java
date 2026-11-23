package data_access;

import entities.Exercise;
import entities.ExerciseSetGroup;
import entities.MeasurementType;
import entities.Template;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
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
        assertEquals(push.getTemplateId(), loaded.getTemplateId());
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

    @Test
    void mixedMeasurementTypesAndFractionalDistancesRoundTrip() {
        Exercise exercise = new Exercise.ExerciseBuilder().id("exercise-1").name("Exercise").build();
        ExerciseSetGroup weights = new ExerciseSetGroup(exercise, MeasurementType.WEIGHT_REPS);
        weights.addWeightRepSet(40.5, 10);
        ExerciseSetGroup timed = new ExerciseSetGroup(exercise, MeasurementType.TIME_DISTANCE);
        timed.addTimeDistanceSet(60, 0.25);
        timed.addTimeDistanceSet(120, 0.75);
        Template template = new Template("stable-id", "Mixed plan");
        template.addGroup(weights);
        template.addGroup(timed);
        template.addGroup(new ExerciseSetGroup(exercise, MeasurementType.TIME_DISTANCE));
        String path = tempDir.resolve("mixed.json").toString();
        new JsonTemplateDataAccessObject(path).save(template);

        Template loaded = new JsonTemplateDataAccessObject(path).get("Mixed plan");
        assertEquals("stable-id", loaded.getTemplateId());
        assertEquals(3, loaded.getGroupCount());
        assertEquals(MeasurementType.WEIGHT_REPS, loaded.getGroup(0).getMeasurementType());
        assertEquals(List.of(40.5), loaded.getGroup(0).getWeights());
        assertEquals(List.of(10), loaded.getGroup(0).getRepsList());
        ExerciseSetGroup loadedTimed = loaded.getGroup(1);
        assertEquals(MeasurementType.TIME_DISTANCE, loadedTimed.getMeasurementType());
        assertEquals(List.of(60, 120), loadedTimed.getTimes());
        assertEquals(List.of(0.25, 0.75), loadedTimed.getDistances());
        assertTrue(loadedTimed.getWeights().isEmpty());
        assertTrue(loadedTimed.getRepsList().isEmpty());
        assertEquals(MeasurementType.TIME_DISTANCE, loaded.getGroup(2).getMeasurementType());
        assertEquals(0, loaded.getGroup(2).getSetCount());
    }

    @Test
    void legacyFilesLoadWithStableIdsAndUpgradeOnSave() throws IOException {
        JSONObject legacy = legacyTemplate();
        Path path = tempDir.resolve("legacy.json");
        String originalJson = new JSONArray().put(legacy).toString();
        Files.writeString(path, originalJson);
        JsonTemplateDataAccessObject firstReader = new JsonTemplateDataAccessObject(path.toString());
        Template first = firstReader.get("Legacy plan");
        Template second = new JsonTemplateDataAccessObject(path.toString()).get("Legacy plan");
        assertFalse(first.getTemplateId().isBlank());
        assertEquals(first.getTemplateId(), second.getTemplateId());
        assertEquals(MeasurementType.WEIGHT_REPS, first.getGroup(0).getMeasurementType());
        assertEquals(List.of(40.0), first.getGroup(0).getWeights());
        assertEquals(List.of(10), first.getGroup(0).getRepsList());
        assertEquals(originalJson, Files.readString(path));

        firstReader.save(first);
        JSONObject upgraded = new JSONArray(Files.readString(path)).getJSONObject(0);
        assertEquals(first.getTemplateId(), upgraded.getString("templateId"));
        assertEquals("WEIGHT_REPS", upgraded.getJSONArray("groups").getJSONObject(0).getString("measurementType"));
        assertEquals(first.getTemplateId(), new JsonTemplateDataAccessObject(path.toString())
                .get("Legacy plan").getTemplateId());
    }

    @Test
    void renamingATemplatePreservesItsIdentityWithoutDuplicatingIt() {
        String path = tempDir.resolve("renamed.json").toString();
        JsonTemplateDataAccessObject repository = new JsonTemplateDataAccessObject(path);
        Template template = new Template("stable-id", "Before");
        repository.save(template);
        template.setName("After");
        repository.save(template);
        assertNull(repository.get("Before"));
        assertEquals(1, repository.getAll().size());
        JsonTemplateDataAccessObject reloaded = new JsonTemplateDataAccessObject(path);
        assertNull(reloaded.get("Before"));
        assertEquals("stable-id", reloaded.get("After").getTemplateId());
        assertEquals(1, reloaded.getAll().size());
    }

    @Test
    void malformedMeasurementPairsAreRejectedWithoutRewritingTheFile() throws IOException {
        Path path = tempDir.resolve("invalid.json");
        JSONObject unequal = legacyTemplate();
        unequal.getJSONArray("groups").getJSONObject(0).put("reps", new JSONArray());
        String original = new JSONArray().put(unequal).toString();
        Files.writeString(path, original);
        assertThrows(IllegalArgumentException.class, () -> new JsonTemplateDataAccessObject(path.toString()));
        assertEquals(original, Files.readString(path));

        JSONObject inactiveValues = legacyTemplate();
        inactiveValues.getJSONArray("groups").getJSONObject(0).put("times", new JSONArray().put(60));
        assertThrows(IllegalArgumentException.class, () -> JsonTemplateDataAccessObject.templateFromJson(inactiveValues));

        JSONObject invalidTimed = legacyTemplate();
        invalidTimed.getJSONArray("groups").getJSONObject(0)
                .put("measurementType", "TIME_DISTANCE")
                .put("weights", new JSONArray()).put("reps", new JSONArray())
                .put("times", new JSONArray().put(60)).put("distances", new JSONArray());
        assertThrows(IllegalArgumentException.class, () -> JsonTemplateDataAccessObject.templateFromJson(invalidTimed));
        JSONObject unknownType = legacyTemplate();
        unknownType.getJSONArray("groups").getJSONObject(0).put("measurementType", "UNKNOWN");
        assertThrows(IllegalArgumentException.class, () -> JsonTemplateDataAccessObject.templateFromJson(unknownType));
    }

    private JSONObject legacyTemplate() {
        JSONObject exercise = new JSONObject().put("id", "bench").put("name", "Bench press");
        JSONObject group = new JSONObject().put("exercise", exercise)
                .put("weights", new JSONArray().put(40.0)).put("reps", new JSONArray().put(10));
        return new JSONObject().put("name", "Legacy plan").put("groups", new JSONArray().put(group));
    }

    @Test
    void jsonIntegersCannotBeSilentlyTruncatedOrOverflowed() {
        JSONObject fractionalReps = legacyTemplate();
        fractionalReps.getJSONArray("groups").getJSONObject(0).put("reps", new JSONArray().put(8.5));
        assertThrows(IllegalArgumentException.class, () -> JsonTemplateDataAccessObject.templateFromJson(fractionalReps));
        for (Number invalid : new Number[]{60.5, 2147483648L}) {
            JSONObject timed = legacyTemplate();
            timed.getJSONArray("groups").getJSONObject(0).put("measurementType", "TIME_DISTANCE")
                    .put("weights", new JSONArray()).put("reps", new JSONArray())
                    .put("times", new JSONArray().put(invalid)).put("distances", new JSONArray().put(0.25));
            assertThrows(IllegalArgumentException.class, () -> JsonTemplateDataAccessObject.templateFromJson(timed));
        }
    }
}
