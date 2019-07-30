package eu.ha3.presencefootsteps.game.interfaces;

import eu.ha3.presencefootsteps.game.Association;
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
