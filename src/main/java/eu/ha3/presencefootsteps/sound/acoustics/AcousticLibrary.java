package eu.ha3.presencefootsteps.sound.acoustics;

import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.world.Association;
import net.minecraft.entity.Entity;

public interface AcousticLibrary {
    /**
     * Adds an acoustic to the library.
     */
    void addAcoustic(String name, Acoustic acoustic);

    /**
     * Plays an acoustic with additional options.
     */
    default void playAcoustic(Association association, State event, Options options) {
        playAcoustic(association.getSource(), association.getAcousticName(), event, options);
    }

    void playAcoustic(Entity location, String acousticName, State event, Options options);

    /**
     * Run various things, such as queued sounds.
     */
    void think();
}