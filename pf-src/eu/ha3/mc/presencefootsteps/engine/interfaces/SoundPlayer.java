package eu.ha3.mc.presencefootsteps.engine.interfaces;

import java.util.Random;

/* x-placeholder-wtfplv2 */

public interface SoundPlayer
{
	/**
	 * Plays a sound.
	 * 
	 * @param location
	 * @param soundName
	 * @param volume
	 * @param pitch
	 * @param options
	 */
	public void playSound(Object location, String soundName, float volume, float pitch, Options options);
	
	/**
	 * Returns a random number generator.
	 * 
	 * @return
	 */
	public Random getRNG();
}
