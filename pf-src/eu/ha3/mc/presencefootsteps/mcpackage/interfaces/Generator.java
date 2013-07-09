package eu.ha3.mc.presencefootsteps.mcpackage.interfaces;

import net.minecraft.src.EntityPlayer;

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

/**
 * Has the ability to generate footsteps based on a Player.
 * 
 * @author Hurry
 * 
 */
public interface Generator
{
	/**
	 * Generate footsteps sounds of the Entity.
	 * 
	 * @param ply
	 */
	public void generateFootsteps(EntityPlayer ply);
}
