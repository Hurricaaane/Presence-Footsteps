package eu.ha3.mc.presencefootsteps.interfaces;

import java.util.Random;

import net.minecraft.src.Block;

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
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param name
	 * @param volume
	 * @param pitch
	 */
	public void playSound(double x, double y, double z, String soundName, float volume, float pitch, Options options);
	
	/**
	 * Play a step sound from a block.
	 * 
	 * @param xx
	 * @param yy
	 * @param zz
	 * @param block
	 */
	public void playStep(long xx, long yy, long zz, Block block);
	
	/**
	 * Returns a random number generator.
	 * 
	 * @return
	 */
	public Random getRNG();
}
