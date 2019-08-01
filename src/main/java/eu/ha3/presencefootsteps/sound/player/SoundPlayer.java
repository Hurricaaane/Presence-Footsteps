package eu.ha3.presencefootsteps.sound.player;

import java.util.Random;

import javax.annotation.Nullable;

import eu.ha3.presencefootsteps.sound.Options;
import net.minecraft.entity.Entity;

public interface SoundPlayer {
    /**
     * Plays a sound.
     */
    void playSound(Entity location, String soundName, float volume, float pitch, @Nullable Options options);

    /**
     * Returns a random number generator.
     */
    Random getRNG();

    void think();
}
