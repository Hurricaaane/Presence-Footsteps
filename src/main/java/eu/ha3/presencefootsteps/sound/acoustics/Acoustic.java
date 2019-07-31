package eu.ha3.presencefootsteps.sound.acoustics;

import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;

/**
 * Something that has the ability to play sounds.
 *
 * @author Hurry
 */
public interface Acoustic {

    Acoustic NULL = (player, location, event, inputOptions) -> {
    };

    /**
     * Plays a sound.
     */
    void playSound(SoundPlayer player, Object location, State event, Options inputOptions);

}