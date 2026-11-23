package data_access;

import entities.Exercise;
import entities.ExerciseSetGroup;
import entities.MeasurementType;
import entities.Template;
import org.json.JSONArray;
import org.json.JSONObject;
import use_case.TemplateDataAccessInterface;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Stores templates in a JSON file, keyed by template name. All templates are
 * loaded into memory on construction and the whole file is rewritten after
 * every change. Each exercise is stored in full so the file is self-contained.
 * Template IDs and measurement types are preserved; older weight/rep files remain readable.
 */
public class JsonTemplateDataAccessObject implements TemplateDataAccessInterface {
    public static final String DEFAULT_PATH = "userdata/templates.json";

    private final Path filePath;
    private final Map<String, Template> templates = new LinkedHashMap<>();

    public JsonTemplateDataAccessObject() {
        this(DEFAULT_PATH);
    }

    public JsonTemplateDataAccessObject(String filePath) {
        this.filePath = Paths.get(filePath);
        load();
    }

    @Override
    public void save(Template template) {
        // Renaming the same template must not leave its old name in storage.
        templates.entrySet().removeIf(entry ->
                entry.getValue().getTemplateId().equals(template.getTemplateId())
                        && !entry.getKey().equals(template.getName()));
        templates.put(template.getName(), template);
        persist();
    }

    @Override
    public Template get(String name) {
        return templates.get(name);
    }

    @Override
    public boolean existsByName(String name) {
        return templates.containsKey(name);
    }

    @Override
    public List<Template> getAll() {
        return List.copyOf(templates.values());
    }

    @Override
    public boolean delete(String name) {
        boolean removed = templates.remove(name) != null;
        if (removed) {
            persist();
        }
        return removed;
    }

    private void load() {
        if (!Files.exists(filePath)) {
            return;
        }
        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            if (content.isBlank()) {
                return;
            }
            JSONArray array = new JSONArray(content);
            for (int i = 0; i < array.length(); i++) {
                Template template = templateFromJson(array.getJSONObject(i));
                templates.put(template.getName(), template);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + filePath, e);
        }
    }

    private void persist() {
        JSONArray array = new JSONArray();
        for (Template template : templates.values()) {
            array.put(templateToJson(template));
        }
        JsonFiles.write(filePath, array.toString(2));
    }

    static JSONObject templateToJson(Template template) {
        JSONArray groups = new JSONArray();
        for (ExerciseSetGroup group : template.getGroups()) {
            JSONObject json = new JSONObject();
            json.put("exercise", exerciseToJson(group.getExercise()));
            json.put("measurementType", group.getMeasurementType().name());
            json.put("weights", new JSONArray(group.getWeights()));
            json.put("reps", new JSONArray(group.getRepsList()));
            json.put("times", new JSONArray(group.getTimes()));
            json.put("distances", new JSONArray(group.getDistances()));
            groups.put(json);
        }
        JSONObject json = new JSONObject();
        json.put("templateId", template.getTemplateId());
        json.put("name", template.getName());
        json.put("groups", groups);
        return json;
    }

    static Template templateFromJson(JSONObject json) {
        String name = json.getString("name");
        // Old files used unique names as identity. A deterministic fallback keeps
        // their IDs stable across reloads without rewriting a file just to read it.
        String id = json.has("templateId") ? json.getString("templateId")
                : UUID.nameUUIDFromBytes(("legacy-template:" + name).getBytes(StandardCharsets.UTF_8)).toString();
        Template template = new Template(id, name);
        JSONArray groups = json.getJSONArray("groups");
        for (int i = 0; i < groups.length(); i++) {
            JSONObject groupJson = groups.getJSONObject(i);
            MeasurementType type = MeasurementType.valueOf(
                    groupJson.optString("measurementType", MeasurementType.WEIGHT_REPS.name()));
            ExerciseSetGroup group = new ExerciseSetGroup(
                    exerciseFromJson(groupJson.getJSONObject("exercise")), type);
            JSONArray weights = measurements(groupJson, "weights");
            JSONArray reps = measurements(groupJson, "reps");
            JSONArray times = measurements(groupJson, "times");
            JSONArray distances = measurements(groupJson, "distances");
            if (type == MeasurementType.WEIGHT_REPS) {
                requirePair(weights, reps, times, distances);
                for (int j = 0; j < weights.length(); j++) {
                    group.addWeightRepSet(weights.getDouble(j), integerMeasurement(reps, j));
                }
            } else {
                requirePair(times, distances, weights, reps);
                for (int j = 0; j < times.length(); j++) {
                    group.addTimeDistanceSet(integerMeasurement(times, j), distances.getDouble(j));
                }
            }
            template.addGroup(group);
        }
        return template;
    }

    private static JSONArray measurements(JSONObject json, String key) {
        return json.has(key) ? json.getJSONArray(key) : new JSONArray();
    }

    private static int integerMeasurement(JSONArray values, int index) {
        try {
            return values.getBigDecimal(index).intValueExact();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Reps and times must be whole numbers within the integer range", exception);
        }
    }

    private static void requirePair(JSONArray first, JSONArray second, JSONArray unusedFirst, JSONArray unusedSecond) {
        if (first.length() != second.length()) {
            throw new IllegalArgumentException("Measurement lists must have matching set counts");
        }
        if (unusedFirst.length() != 0 || unusedSecond.length() != 0) {
            throw new IllegalArgumentException("Unused measurement lists must be empty for the group's type");
        }
    }

    private static JSONObject exerciseToJson(Exercise exercise) {
        JSONObject json = new JSONObject();
        json.put("id", exercise.getId());
        json.put("name", exercise.getName());
        json.put("targetMuscles", new JSONArray(exercise.getTargetMuscles()));
        json.put("equipments", new JSONArray(exercise.getEquipments()));
        json.put("bodyParts", new JSONArray(exercise.getBodyParts()));
        json.put("videoUrl", exercise.getVideoUrl() == null ? JSONObject.NULL : exercise.getVideoUrl());
        json.put("instructions", new JSONArray(exercise.getInstructions()));
        return json;
    }

    private static Exercise exerciseFromJson(JSONObject json) {
        return new Exercise.ExerciseBuilder()
                .id(json.getString("id"))
                .name(json.getString("name"))
                .targetMuscles(toStringList(json.optJSONArray("targetMuscles")))
                .equipments(toStringList(json.optJSONArray("equipments")))
                .bodyParts(toStringList(json.optJSONArray("bodyParts")))
                .videoUrl(json.isNull("videoUrl") ? null : json.getString("videoUrl"))
                .instructions(toStringList(json.optJSONArray("instructions")))
                .build();
    }

    private static List<String> toStringList(JSONArray array) {
        List<String> result = new ArrayList<>();
        if (array == null) {
            return result;
        }
        for (int i = 0; i < array.length(); i++) {
            result.add(array.getString(i));
        }
        return result;
    }
}
