package data_access.SearchExercise;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import entities.Exercise;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gateway responsible for communicating with the external ExerciseDB V1 API
 * over HTTP and mapping JSON responses into {@link Exercise} entities.
 */
public class ExternalExerciseApiGateway {

    private static final String DEFAULT_BASE_URL = "https://oss.exercisedb.dev/api/v1";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private static final int MAX_PAGES = 200;

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Creates a gateway using the default ExerciseDB V1 base URL.
     */
    public ExternalExerciseApiGateway() {
        this(DEFAULT_BASE_URL);
    }

    /**
     * Creates a gateway with a custom base URL (useful for testing or staging).
     *
     * @param baseUrl the root API URL without a trailing slash
     */
    public ExternalExerciseApiGateway(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetches all exercises from the external API, following cursor pagination
     * while {@code meta.hasNextPage} is true.
     *
     * @return combined list of {@link Exercise} entities across all pages
     * @throws RuntimeException if a request fails, returns an error status,
     *                          or returns an unexpected response shape
     */
    public List<Exercise> fetchAllExercises() {
        List<Exercise> allExercises = new ArrayList<>();
        Set<String> visitedCursors = new HashSet<>();
        String cursor = null;
        int pageCount = 0;

        while (true) {
            pageCount++;
            if (pageCount > MAX_PAGES) {
                throw new RuntimeException("Exceeded maximum pagination pages (" + MAX_PAGES + ").");
            }

            String path = buildExercisesPath(cursor);
            String responseBody = sendGetRequest(path, false);

            try {
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode items = extractItemsArray(root);

                for (JsonNode item : items) {
                    allExercises.add(mapToExercise(item));
                }

                JsonNode meta = root.path("meta");
                boolean hasNextPage = meta.path("hasNextPage").asBoolean(false);
                if (!hasNextPage) {
                    break;
                }

                String nextCursor = extractNextCursor(root, meta);
                if (nextCursor == null || nextCursor.isBlank()) {
                    throw new RuntimeException(
                            "API reported hasNextPage=true but provided no nextCursor.");
                }
                if (!visitedCursors.add(nextCursor)) {
                    throw new RuntimeException("Detected repeated pagination cursor: " + nextCursor);
                }
                cursor = nextCursor;
            } catch (IOException ex) {
                throw new RuntimeException("Failed to parse exercises list from API response.", ex);
            }
        }

        return allExercises;
    }

    /**
     * Fetches a single exercise by its identifier from the external API.
     *
     * @param exerciseId the unique ID of the exercise
     * @return the matching {@link Exercise}, or {@code null} if not found (HTTP 404)
     * @throws RuntimeException if the network request fails or returns an unexpected error status
     */
    public Exercise fetchExerciseById(String exerciseId) {
        String encodedId = URLEncoder.encode(exerciseId, StandardCharsets.UTF_8);
        String responseBody = sendGetRequest("/exercises/" + encodedId, true);
        if ("null".equals(responseBody)) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode exerciseNode = root.has("data") && !root.get("data").isNull()
                    ? root.get("data")
                    : root;
            if (exerciseNode == null || exerciseNode.isNull() || exerciseNode.isMissingNode()) {
                return null;
            }
            return mapToExercise(exerciseNode);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to parse exercise details for ID: " + exerciseId, ex);
        }
    }

    private String buildExercisesPath(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return "/exercises";
        }
        String encodedCursor = URLEncoder.encode(cursor, StandardCharsets.UTF_8);
        return "/exercises?after=" + encodedCursor;
    }

    private JsonNode extractItemsArray(JsonNode root) {
        if (root != null && root.isArray()) {
            return root;
        }
        if (root != null && root.has("data") && root.get("data").isArray()) {
            return root.get("data");
        }
        throw new RuntimeException("Unexpected exercises list response shape from API.");
    }

    private String extractNextCursor(JsonNode root, JsonNode meta) {
        String metaCursor = meta.path("nextCursor").asText("");
        if (!metaCursor.isBlank()) {
            return metaCursor;
        }
        return root.path("nextCursor").asText("");
    }

    private String sendGetRequest(String path, boolean allowNotFoundAsNull) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            if (status == 404 && allowNotFoundAsNull) {
                return "null";
            }
            if (status == 429) {
                throw new RuntimeException("ExerciseDB API rate limit exceeded (HTTP 429).");
            }
            if (status < 200 || status >= 300) {
                throw new RuntimeException(
                        "ExerciseDB API request failed with HTTP status " + status + ".");
            }
            return response.body();
        } catch (IOException ex) {
            throw new RuntimeException("Network error while calling ExerciseDB API: " + path, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Request interrupted while calling ExerciseDB API: " + path, ex);
        }
    }

    private Exercise mapToExercise(JsonNode node) {
        String id = firstNonBlankText(node, "exerciseId", "id");
        String name = node.path("name").asText("");
        List<String> targetMuscles = readStringListOrSingle(node, "targetMuscles", "targetMuscle");
        List<String> equipments = readStringListOrSingle(node, "equipments", "equipment");
        List<String> bodyParts = readStringListOrSingle(node, "bodyParts", "bodyPart");
        String videoUrl = firstNonBlankText(node, "gifUrl", "videoUrl");
        List<String> instructions = readStringArray(node.path("instructions"));

        return new Exercise.ExerciseBuilder()
                .id(id)
                .name(name)
                .targetMuscles(targetMuscles)
                .equipments(equipments)
                .bodyParts(bodyParts)
                .videoUrl(videoUrl)
                .instructions(instructions)
                .build();
    }

    private String firstNonBlankText(JsonNode node, String primaryField, String fallbackField) {
        String primary = node.path(primaryField).asText("");
        if (!primary.isEmpty()) {
            return primary;
        }
        return node.path(fallbackField).asText("");
    }

    private List<String> readStringListOrSingle(JsonNode node, String listField, String singleField) {
        JsonNode listNode = node.path(listField);
        if (listNode.isArray()) {
            return readStringArray(listNode);
        }
        String singleValue = node.path(singleField).asText("");
        if (!singleValue.isEmpty()) {
            return List.of(singleValue);
        }
        return Collections.emptyList();
    }

    private List<String> readStringArray(JsonNode arrayNode) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return Collections.emptyList();
        }
        List<String> values = new ArrayList<>();
        for (JsonNode element : arrayNode) {
            values.add(element.asText(""));
        }
        return values;
    }
}