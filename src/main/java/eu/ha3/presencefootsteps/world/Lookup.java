package eu.ha3.presencefootsteps.world;

import javax.annotation.Nullable;

import eu.ha3.presencefootsteps.PresenceFootsteps;
import eu.ha3.presencefootsteps.config.ConfigReader;

public interface Lookup<T> {

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
     * Clears the contents of this lookup table.
     */
    void clear();

    /**
     * Loads new entries from the given config reader.
     * The read values will added to any existing ones.
     */
    default void load(ConfigReader stream) {
        stream.properties().forEach(property -> {
            try {
                add(property.getName(), property.getString());
            } catch (Exception e) {
                PresenceFootsteps.logger.error("Error when loading lookup values", e);
            }
        });
    }
}