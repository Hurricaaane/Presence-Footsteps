package eu.ha3.mc.presencefootsteps.engine.interfaces;

import java.util.Random;

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

public interface SoundPlayer
{
	/**
	 * Plays a sound.
	 * @param volume
	 * @param pitch
	 * @param name
	 */
	public void playSound(Object location, String soundName, float volume, float pitch, Options options);
	
	/**
	 * Returns a random number generator.
	 * 
	 * @return
	 */
	public Random getRNG();
}
