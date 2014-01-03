package net.minecraft.src;

import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.DefaultStepPlayer;

/* x-placeholder-wtfplv2 */

/**
 * An accessor to non-visible methods from this package.
 * 
 * @author Hurry
 * 
 */
public class PFAccessors implements DefaultStepPlayer
{
	private static final PFAccessors instance = new PFAccessors();
	
	private PFAccessors()
	{
	}
	
	public static PFAccessors getInstance()
	{
		return instance;
	}
	
	@Override
	public void playStep(EntityLivingBase entity, int xx, int yy, int zz, int blockID)
	{
		entity.playStepSound(xx, yy, zz, blockID);
	}
}
