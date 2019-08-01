package eu.ha3.presencefootsteps.sound.player;

import java.util.Random;

import eu.ha3.presencefootsteps.PFConfig;
import eu.ha3.presencefootsteps.sound.Options;
import net.minecraft.entity.Entity;

public class UserConfigSoundPlayerWrapper implements SoundPlayer {

    private final SoundPlayer wrapped;

    private final PFConfig userConfig;

    public UserConfigSoundPlayerWrapper(SoundPlayer player, PFConfig config) {
        userConfig = config;
        wrapped = player;
    }

    @Override
    public void playSound(Entity location, String soundName, float volume, float pitch, Options options) {
        wrapped.playSound(location, soundName, volume * userConfig.getVolume() / 100F, pitch, options);
    }

    @Override
    public Random getRNG() {
        return wrapped.getRNG();
    }

    @Override
    public void think() {
        wrapped.think();
    }
}
