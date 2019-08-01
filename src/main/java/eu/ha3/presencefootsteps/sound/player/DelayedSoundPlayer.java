package eu.ha3.presencefootsteps.sound.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eu.ha3.presencefootsteps.sound.Options;
import net.minecraft.entity.Entity;

class DelayedSoundPlayer implements SoundPlayer {

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
    public void playSound(Entity location, String soundName, float volume, float pitch, Options options) {
        pending.add(new PendingSound(location, soundName, getRNG(), volume, pitch, options, nextPlayTime));
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
        case PLAYING:
            sound.play(immediate);
            return false;
        case SKIPPING:
            return true;
        default:
            nextPlayTime = sound.getTimeToPlay();
            return false;
        }
    }
}
