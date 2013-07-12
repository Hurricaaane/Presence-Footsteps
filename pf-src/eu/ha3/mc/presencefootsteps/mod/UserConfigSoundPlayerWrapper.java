package eu.ha3.mc.presencefootsteps.mod;

import java.util.Random;

import eu.ha3.mc.presencefootsteps.engine.interfaces.Options;
import eu.ha3.mc.presencefootsteps.engine.interfaces.SoundPlayer;
import eu.ha3.util.property.contract.PropertyHolder;

/*
            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
                    Version 2, December 2004 

 Copyright (C) 2004 Sam Hocevar <sam@hocevar.net> 

 Everyone is permitted to copy and distribute verbatim or modified 
 copies of this license document, and changing it is allowed as long 
 as the name is changed. 

            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION 

  0. You just DO WHAT THE FUCK YOU WANT TO. 
*/

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
		this.wrapped.playSound(location, soundName, volume * this.userConfig.getFloat("user.vol_mod"), pitch, options);
	}
	
	@Override
	public Random getRNG()
	{
		return this.wrapped.getRNG();
	}
	
}
