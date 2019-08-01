package eu.ha3.presencefootsteps.sound.player;

import eu.ha3.presencefootsteps.PresenceFootsteps;
import eu.ha3.presencefootsteps.sound.Options;
import net.minecraft.entity.Entity;

class PendingSound {

    private static final boolean USING_LATENESS = true;
    private static final boolean USING_EARLYNESS = true;

    private static final float LATENESS_THRESHOLD_DIVIDER = 1.5f;

    private static final double EARLYNESS_THRESHOLD_POW = 0.75d;

    private Entity location;

    private String soundName;

    private float volume;
    private float pitch;

    private Options options;

    private long timeToPlay;
    private long maximum;

    public PendingSound(Entity location, String soundName, float volume, float pitch, Options options, long timeToPlay, long maximum) {
        this.location = location;
        this.soundName = soundName;
        this.volume = volume;
        this.pitch = pitch;
        this.options = options;

        this.timeToPlay = timeToPlay;
        this.maximum = maximum;
    }

    public int nextState(long time) {
        if (time >= getTimeToPlay() || USING_EARLYNESS
                && time >= getTimeToPlay() - Math.pow(getMaximumBase(), EARLYNESS_THRESHOLD_POW)) {
            if (USING_EARLYNESS && time < getTimeToPlay()) {
                PresenceFootsteps.logger.debug("Playing early sound (early by " + (getTimeToPlay() - time) + "ms, tolerence is " + Math.pow(getMaximumBase(), EARLYNESS_THRESHOLD_POW));
            }

            long lateness = time - getTimeToPlay();
            if (!USING_LATENESS
                    || getMaximumBase() < 0
                    || lateness <= getMaximumBase() / LATENESS_THRESHOLD_DIVIDER) {
                return 0;
            }

            PresenceFootsteps.logger.debug("Skipped late sound (late by " + lateness + "ms, tolerence is " + getMaximumBase() / LATENESS_THRESHOLD_DIVIDER + "ms)");

            return 1;
        }

        return 2;
    }

    /**
     * Play the sound stored in this pending sound.
     */
    public void playSound(SoundPlayer player) {
        player.playSound(location, soundName, volume, pitch, options);
    }

    /**
     * Returns the time after which this sound plays.
     */
    public long getTimeToPlay() {
        return timeToPlay;
    }

    /**
     * Get the maximum delay of this sound, for threshold purposes. If the value is
     * negative, the sound will not be skippable.
     */
    public long getMaximumBase() {
        return maximum;
    }
}
