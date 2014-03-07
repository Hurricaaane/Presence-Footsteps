package eu.ha3.mc.presencefootsteps.mcpackage.interfaces;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;

/* x-placeholder-wtfplv2 */

/**
 * Can generate footsteps using the default Minecraft function.
 * 
 * @author Hurry
 */
public interface DefaultStepPlayer
{
	/**
	 * Play a step sound from a block.
	 * 
	 * @param entity
	 * @param xx
	 * @param yy
	 * @param zz
	 * @param blockID
	 */
	public void playStep(EntityLivingBase entity, int xx, int yy, int zz, Block blockID);
}
