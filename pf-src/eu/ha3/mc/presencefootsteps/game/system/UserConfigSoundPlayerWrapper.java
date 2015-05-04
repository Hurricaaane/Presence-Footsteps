package eu.ha3.mc.presencefootsteps.game.system;

import java.util.Random;

import eu.ha3.mc.presencefootsteps.engine.interfaces.Options;
import eu.ha3.mc.presencefootsteps.engine.interfaces.SoundPlayer;
import eu.ha3.util.property.contract.PropertyHolder;

public class UserConfigSoundPlayerWrapper implements SoundPlayer {
	private SoundPlayer wrapped;
	private PropertyHolder userConfig;
	
	public UserConfigSoundPlayerWrapper(SoundPlayer player, PropertyHolder config) {
		userConfig = config;
		wrapped = player;
	}
	
	@Override
	public void playSound(Object location, String soundName, float volume, float pitch, Options options) {
		wrapped.playSound(location, soundName, volume * userConfig.getInteger("user.volume") / 100f, pitch, options);
	}
	
	@Override
	public Random getRNG() {
		return wrapped.getRNG();
	}
	
}
