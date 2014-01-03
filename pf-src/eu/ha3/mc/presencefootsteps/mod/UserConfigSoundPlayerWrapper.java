package eu.ha3.mc.presencefootsteps.mod;

import java.util.Random;

import eu.ha3.mc.presencefootsteps.engine.interfaces.Options;
import eu.ha3.mc.presencefootsteps.engine.interfaces.SoundPlayer;
import eu.ha3.util.property.contract.PropertyHolder;

/* x-placeholder-wtfplv2 */

public class UserConfigSoundPlayerWrapper implements SoundPlayer
{
	private SoundPlayer wrapped;
	private PropertyHolder userConfig;
	
	public UserConfigSoundPlayerWrapper(SoundPlayer wrapped, PropertyHolder userConfig)
	{
		this.userConfig = userConfig;
		this.wrapped = wrapped;
	}
	
	@Override
	public void playSound(Object location, String soundName, float volume, float pitch, Options options)
	{
		this.wrapped.playSound(
			location, soundName, volume * this.userConfig.getInteger("user.volume.0-to-100") / 100f, pitch, options);
	}
	
	@Override
	public Random getRNG()
	{
		return this.wrapped.getRNG();
	}
	
}
