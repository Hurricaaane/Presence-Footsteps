package eu.ha3.presencefootsteps.world;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public interface Lookup<T> {

    String EMPTY_SUBSTRATE = "";
    String CARPET_SUBSTRATE = "carpet";
    String FENCE_SUBSTRATE = "bigger";
    String FOLIAGE_SUBSTRATE = "foliage";
    String MESSY_SUBSTRATE = "messy";

    Gson GSON = new Gson();

    /**
     * This will return the appropriate association for the given state and substrate.
     *
     * Returns Emitter.UNASSIGNED when no mapping exists,
     * or Emitter.NOT_EMITTER if such a mapping exists and produces no sound.
     */
    String getAssociation(T state, String substrate);

    /**
     * Gets a set of all the substrates this map contains entries for.
     */
    Set<String> getSubstrates();

    /**
     * Gets all the associations for the given state.
     */
    default Map<String, String> getAssociations(T state) {
        Map<String, String> result = new HashMap<>();

        for (String substrate : getSubstrates()) {
            String association = getAssociation(state, substrate);

            if (Emitter.isResult(association)) {
                result.put(substrate, association);
            }
        }

        return result;
    }

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
        GSON.fromJson(reader, JsonObject.class).entrySet().forEach(entry -> {
            add(entry.getKey(), entry.getValue().getAsString());
        });
    }
}