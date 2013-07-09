package net.minecraft.src;

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

public interface PFDefaultStepPlayer
{
	/**
	 * Play a step sound from a block.
	 * 
	 * @param entity
	 *            TODO
	 * @param xx
	 * @param yy
	 * @param zz
	 * @param block
	 */
	public void playStep(EntityLivingBase entity, int xx, int yy, int zz, int blockID);
}
