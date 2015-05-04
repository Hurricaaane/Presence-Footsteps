package eu.ha3.mc.presencefootsteps.mcpackage.interfaces;

import eu.ha3.mc.presencefootsteps.game.system.Association;
import net.minecraft.entity.EntityLivingBase;

/**
 * Can generate footsteps using the default Minecraft function.
 */
public interface DefaultStepPlayer {
	/**
	 * Play a step sound from a block.
	 */
	public void playStep(EntityLivingBase entity, Association non);
}
