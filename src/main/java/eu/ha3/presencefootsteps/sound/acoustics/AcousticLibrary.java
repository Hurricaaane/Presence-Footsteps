package eu.ha3.presencefootsteps.sound.acoustics;

import eu.ha3.presencefootsteps.config.Options;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.world.Association;

public interface AcousticLibrary {
    /**
     * Adds an acoustic to the library.
     */
    void addAcoustic(NamedAcoustic acoustic);

    /**
     * Plays an acoustic.
     */
    default void playAcoustic(Object location, Association acousticName, State event) {
        playAcoustic(location, acousticName, event, null);
    }

    /**
     * Plays an acoustic with additional options.
     */
    default void playAcoustic(Object location, Association acousticName, State event, Options options) {
        playAcoustic(location, acousticName.getData(), event, options);
    }

    void playAcoustic(Object location, String acousticName, State event, Options options);

    /**
     * Run various things, such as queued sounds.
     */
    void think();
}