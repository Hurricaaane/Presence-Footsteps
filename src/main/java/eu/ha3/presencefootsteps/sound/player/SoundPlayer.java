package eu.ha3.presencefootsteps.sound.player;

import java.util.Random;

import javax.annotation.Nullable;

import eu.ha3.presencefootsteps.sound.Options;

public interface SoundPlayer {
    /**
     * Plays a sound.
     */
    void playSound(Object location, String soundName, float volume, float pitch, @Nullable Options options);

    /**
     * Returns a random number generator.
     */
    Random getRNG();

    void think();
}
