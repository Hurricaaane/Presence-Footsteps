package eu.ha3.presencefootsteps.world;

import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public interface Loadable {
    Gson GSON = new Gson();

    /**
     * Register a blockmap entry.
     */
    void add(String key, String value);

    /**
     * Loads new entries from the given config reader.
     * The read values will added to any existing ones.
     */
    default void load(Reader reader) {
        GSON.fromJson(reader, JsonObject.class).entrySet().forEach(entry -> {
            add(entry.getKey(), entry.getValue().getAsString());
        });
    }
}
