package eu.ha3.presencefootsteps.world;

import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public interface Lookup<T> {

    String EMPTY_SUBSTRATE = "";

    Gson gson = new Gson();

    /**
     * This will return the appropriate association for the given state and substrate.
     *
     * Returns Emitter.UNASSIGNED when no mapping exists,
     * or Emitter.NOT_EMITTER if such a mapping exists and produces no sound.
     */
    String getAssociation(T state, String substrate);

    /**
     * Register a blockmap entry.
     */
    void add(String key, String value);

    /**
     * Returns true if this lookup contains a mapping for the given value.
     */
    boolean contains(T state);

    /**
     * Loads new entries from the given config reader.
     * The read values will added to any existing ones.
     */
    default void load(Reader reader) {
        gson.fromJson(reader, JsonObject.class).entrySet().forEach(entry -> {
            add(entry.getKey(), entry.getValue().getAsString());
        });
    }
}