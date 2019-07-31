package eu.ha3.presencefootsteps.sound.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eu.ha3.presencefootsteps.config.Options;
import eu.ha3.presencefootsteps.util.MathUtil;

public class DelayedSoundPlayer implements SoundPlayer {

    private final List<PendingSound> pending = new ArrayList<>();

    private final SoundPlayer immediate;

    private long nextPlayTime;
    private long currentTime;

    public DelayedSoundPlayer(SoundPlayer immediate) {
        this.immediate = immediate;
    }

    @Override
    public Random getRNG() {
        return immediate.getRNG();
    }

    @Override
    public void playSound(Object location, String soundName, float volume, float pitch, Options options) {
        long delay = MathUtil.randAB(getRNG(), options.get("delay_min"), options.get("delay_max"));

        delay = Math.max(delay, nextPlayTime);

        pending.add(new PendingSound(location, soundName, volume, pitch, null, System.currentTimeMillis() + delay,
                        options.containsKey("skippable") ? -1L : options.get("delay_max")));
    }

    @Override
    public void think() {
        currentTime = System.currentTimeMillis();

        if (pending.isEmpty() || currentTime < nextPlayTime) {
            return;
        }

        nextPlayTime = Long.MAX_VALUE;

        pending.removeIf(this::tickPendingSound);
    }

    private boolean tickPendingSound(PendingSound sound) {
        switch (sound.nextState(currentTime)) {
        case 0:
            sound.playSound(immediate);
            return false;
        case 1:
            return true;
        default:
            nextPlayTime = sound.getTimeToPlay();
            return false;
        }
    }
}
