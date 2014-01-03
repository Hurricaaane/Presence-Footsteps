package eu.ha3.mc.presencefootsteps.mcpackage.interfaces;

import net.minecraft.src.EntityPlayer;

/* x-placeholder-wtfplv2 */

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
