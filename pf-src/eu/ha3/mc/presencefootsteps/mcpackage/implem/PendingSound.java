package eu.ha3.mc.presencefootsteps.mcpackage.implem;

import eu.ha3.mc.presencefootsteps.engine.interfaces.Options;
import eu.ha3.mc.presencefootsteps.engine.interfaces.SoundPlayer;

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

public class PendingSound
{
	private Object location;
	private String soundName;
	private float volume;
	private float pitch;
	private Options options;
	private long timeToPlay;
	private long maximum;
	
	public PendingSound(
		Object location, String soundName, float volume, float pitch, Options options, long timeToPlay, long maximum)
	{
		this.location = location;
		this.soundName = soundName;
		this.volume = volume;
		this.pitch = pitch;
		this.options = options;
		
		this.timeToPlay = timeToPlay;
		this.maximum = maximum;
	}
	
	/**
	 * Play the sound stored in this pending sound.
	 * 
	 * @param player
	 */
	public void playSound(SoundPlayer player)
	{
		player.playSound(this.location, this.soundName, this.volume, this.pitch, this.options);
	}
	
	/**
	 * Returns the time after which this sound plays.
	 * 
	 * @return
	 */
	public long getTimeToPlay()
	{
		return this.timeToPlay;
	}
	
	/**
	 * Get the maximum delay of this sound, for threshold purposes. If the value
	 * is negative, the sound will not be skippable.
	 * 
	 * @return
	 */
	public long getMaximumBase()
	{
		return this.maximum;
	}
}
