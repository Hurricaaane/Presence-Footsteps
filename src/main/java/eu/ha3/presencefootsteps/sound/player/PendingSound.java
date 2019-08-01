package eu.ha3.presencefootsteps.sound.player;

import java.util.Random;

import eu.ha3.presencefootsteps.PresenceFootsteps;
import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.util.MathUtil;
import net.minecraft.entity.Entity;

class PendingSound {

    private static final boolean USING_LATENESS = true;
    private static final boolean USING_EARLYNESS = true;

    private static final float LATENESS_THRESHOLD = 1.5f;

    private static final double EARLYNESS_THRESHOLD_POW = 0.75d;

    private final Entity location;

    private final String soundName;

    private final float volume;
    private final float pitch;

    private final long timeToPlay;
    private final long maximum;

    public PendingSound(Entity location, String soundName, Random rng, float volume, float pitch, Options options, long nextPlayTime) {
        this.location = location;
        this.soundName = soundName;
        this.volume = volume;
        this.pitch = pitch;

        maximum = options.containsKey("skippable") ? -1L : options.get("delay_max");
        timeToPlay = System.currentTimeMillis() + Math.max(MathUtil.randAB(rng,
                options.get("delay_min"),
                options.get("delay_max")
        ), nextPlayTime);
    }

    public State nextState(long time) {
        if (time >= timeToPlay || USING_EARLYNESS
         && time >= timeToPlay - Math.pow(maximum, EARLYNESS_THRESHOLD_POW)) {
            if (USING_EARLYNESS && time < timeToPlay) {
                PresenceFootsteps.logger.debug("Playing early sound (early by " + (timeToPlay - time) + "ms, tolerence is " + Math.pow(maximum, EARLYNESS_THRESHOLD_POW));
            }

            long lateness = time - timeToPlay;

            if (!USING_LATENESS
                    || maximum < 0
                    || lateness <= maximum / LATENESS_THRESHOLD) {
                return State.PLAYING;
            }

            PresenceFootsteps.logger.debug("Skipped late sound (late by " + lateness + "ms, tolerence is " + maximum / LATENESS_THRESHOLD + "ms)");

            return State.SKIPPING;
        }

        return State.PENDING;
    }

    /**
     * Play the sound stored in this pending sound.
     */
    public void play(SoundPlayer player) {
        player.playSound(location, soundName, volume, pitch, Options.EMPTY);
    }

    /**
     * Returns the time after which this sound plays.
     */
    public long getTimeToPlay() {
        return timeToPlay;
    }

    enum State {
        PENDING,
        PLAYING,
        SKIPPING
    }
}
