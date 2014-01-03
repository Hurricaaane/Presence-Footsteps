package eu.ha3.mc.presencefootsteps.engine.interfaces;

/* x-placeholder-wtfplv2 */

/**
 * Something that has the ability to play sounds.
 * 
 * @author Hurry
 * 
 */
public interface Acoustic
{
	/**
	 * Plays a sound.
	 * 
	 * @param player
	 * @param x
	 * @param y
	 * @param z
	 * @param event
	 */
	public void playSound(SoundPlayer player, Object location, EventType event, Options inputOptions);
	
}