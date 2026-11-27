package use_case;

import entities.Template;

import java.io.UncheckedIOException;
import java.util.List;

/**
 * Storage for workout templates.
 * A template's name is its unique key.
 * Implementations preserve its stable templateId when saving or loading it.
 */
public interface TemplateDataAccessInterface {

    /**
     * Saves a template, overwriting any existing template with the same name.
     * @throws UncheckedIOException if the template could not be written to storage
     */
    void save(Template template);

    /** @return the template with the given name, or null if none exists. */
    Template get(String name);

    /** @return true if a template with the given name exists. */
    boolean existsByName(String name);

    /** @return all saved templates. */
    List<Template> getAll();

    /**
     * Deletes the template with the given name.
     * @return true if a template was deleted, false if none matched.
     */
    boolean delete(String name);

    /**
     * Reads a standalone template JSON file without saving it to the app's storage.
     * @throws UncheckedIOException if the file could not be read
     * @throws IllegalArgumentException if the file does not describe a valid template
     */
    Template readFromFile(String path);

    /**
     * Writes a standalone template JSON file at the chosen path.
     * @throws UncheckedIOException if the export file could not be written
     * @throws IllegalArgumentException if the path points at the app's template storage
     */
    void writeToFile(Template template, String path);
}
