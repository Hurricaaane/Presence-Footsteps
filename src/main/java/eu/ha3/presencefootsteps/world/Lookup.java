package eu.ha3.presencefootsteps.world;

import java.io.Reader;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public interface Lookup<T> {

    Gson gson = new Gson();

    /**
     * This will return null if the block is not defined, and NOT_EMITTER if the
     * block is a non-emitting block, meaning block resolution must continue on its
     * neighbours.
     */
    @Nullable
    String getAssociation(T state);

    /**
     * This will return null if the substrate does not resolve in the selected
     * carpet.
     */
    @Nullable
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