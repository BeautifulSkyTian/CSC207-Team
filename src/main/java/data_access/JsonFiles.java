package data_access;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Shared file-writing helper for the JSON data access objects. */
final class JsonFiles {
    private JsonFiles() {
    }

    /** Writes the text to the file, creating parent directories (e.g. userdata/) if needed. */
    static void write(Path filePath, String content) {
        try {
            Path parent = filePath.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(filePath, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write " + filePath, e);
        }
    }

    /** Writes a complete replacement beside the target before publishing it. */
    static void replace(Path filePath, String content) {
        Path target = filePath.toAbsolutePath();
        try {
            Path directory = Files.createDirectories(target.getParent());
            Path replacement = Files.createTempFile(directory, "json-", ".tmp");
            try {
                Files.writeString(replacement, content, StandardCharsets.UTF_8);
                try {
                    Files.move(replacement, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(replacement, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(replacement);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write " + filePath, e);
        }
    }
}
