package net.minecraft.entity;

import net.minecraft.block.Block;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.DefaultStepPlayer;

/*
--filenotes-placeholder
*/

public class PFAccessor_NetMinecraftEntity implements DefaultStepPlayer
{
	private static final PFAccessor_NetMinecraftEntity instance = new PFAccessor_NetMinecraftEntity();
	
	private PFAccessor_NetMinecraftEntity()
	{
	}
	
	public static PFAccessor_NetMinecraftEntity getInstance()
	{
		return instance;
	}
	
	public boolean isJumping(EntityLivingBase entityLiving)
	{
		return entityLiving.isJumping;
	}
	
	@Override
	public void playStep(EntityLivingBase entity, int xx, int yy, int zz, Block blockID)
	{
		//playStepSound
		entity.func_145780_a(xx, yy, zz, blockID);
	}
}
