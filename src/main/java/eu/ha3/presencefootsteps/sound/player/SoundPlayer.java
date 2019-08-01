package eu.ha3.presencefootsteps.sound.player;

import java.util.Random;

import eu.ha3.presencefootsteps.sound.Options;
import net.minecraft.entity.Entity;

public interface SoundPlayer {
    /**
     * Plays a sound.
     */
    void playSound(Entity location, String soundName, float volume, float pitch, Options options);

    /**
     * Returns a random number generator.
     */
    Random getRNG();

    void think();
}
