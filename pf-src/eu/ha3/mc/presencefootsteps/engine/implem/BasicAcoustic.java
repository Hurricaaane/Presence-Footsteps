package eu.ha3.mc.presencefootsteps.engine.implem;

import java.util.Random;

import eu.ha3.mc.presencefootsteps.engine.interfaces.Acoustic;
import eu.ha3.mc.presencefootsteps.engine.interfaces.EventType;
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

public class BasicAcoustic implements Acoustic
{
	protected String soundName;
	protected float volMin = 1f;
	protected float volMax = 1f;
	protected float pitchMin = 1f;
	protected float pitchMax = 1f;
	
	protected Options outputOptions;
	
	@Override
	public void playSound(SoundPlayer player, Object location, EventType event, Options inputOptions)
	{
		// Special case for intentionnaly empty sounds (as opposed to fallback sounds)
		if (this.soundName.equals(""))
			return;
		
		float volume = generateVolume(player.getRNG());
		float pitch = generatePitch(player.getRNG());
		if (inputOptions != null)
		{
			if (inputOptions.hasOption("gliding_volume"))
			{
				volume = this.volMin + (this.volMax - this.volMin) * (Float) inputOptions.getOption("gliding_volume");
			}
			if (inputOptions.hasOption("gliding_pitch"))
			{
				pitch =
					this.pitchMin + (this.pitchMax - this.pitchMin) * (Float) inputOptions.getOption("gliding_pitch");
			}
		}
		player.playSound(location, this.soundName, volume, pitch, this.outputOptions);
	}
	
	private float generateVolume(Random rng)
	{
		return randAB(rng, this.volMin, this.volMax);
	}
	
	private float generatePitch(Random rng)
	{
		return randAB(rng, this.pitchMin, this.pitchMax);
	}
	
	private float randAB(Random rng, float a, float b)
	{
		if (a >= b)
			return a;
		
		return a + rng.nextFloat() * (b - a);
	}
	
	//
	
	public void setSoundName(String soundName)
	{
		this.soundName = soundName;
	}
	
	public void setVolMin(float volMin)
	{
		this.volMin = volMin;
	}
	
	public void setVolMax(float volMax)
	{
		this.volMax = volMax;
	}
	
	public void setPitchMin(float pitchMin)
	{
		this.pitchMin = pitchMin;
	}
	
	public void setPitchMax(float pitchMax)
	{
		this.pitchMax = pitchMax;
	}
	
}
