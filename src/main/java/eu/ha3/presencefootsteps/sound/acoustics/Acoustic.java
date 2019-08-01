package eu.ha3.presencefootsteps.sound.acoustics;

import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import net.minecraft.entity.Entity;

/**
 * Something that has the ability to play sounds.
 *
 * @author Hurry
 */
public interface Acoustic {
    /**
     * Plays a sound.
     */
    void playSound(SoundPlayer player, Entity location, State event, Options inputOptions);

}