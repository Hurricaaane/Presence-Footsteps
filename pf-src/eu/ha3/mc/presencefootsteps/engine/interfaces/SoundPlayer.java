package eu.ha3.mc.presencefootsteps.engine.interfaces;

import java.util.Random;

public interface SoundPlayer {
	/**
	 * Plays a sound.
	 */
	public void playSound(Object location, String soundName, float volume, float pitch, Options options);
	
	/**
	 * Returns a random number generator.
	 */
	public Random getRNG();
}
