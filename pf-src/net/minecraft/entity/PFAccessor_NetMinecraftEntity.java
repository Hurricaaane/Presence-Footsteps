package net.minecraft.entity;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
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
	
	@Override
	public void playStep(EntityLivingBase entity, int xx, int yy, int zz, Block blockID)
	{
		//playStepSound
		//entity.func_145780_a(xx, yy, zz, blockID);
		
		Block.SoundType soundType = blockID.stepSound;
		
		if (Minecraft.getMinecraft().theWorld.getBlock(xx, yy + 1, zz) == Blocks.snow_layer)
		{
			soundType = Blocks.snow_layer.stepSound;
			entity.playSound(soundType.func_150498_e(), soundType.func_150497_c() * 0.15F, soundType.func_150494_d());
		}
		else if (!blockID.getMaterial().isLiquid())
		{
			entity.playSound(soundType.func_150498_e(), soundType.func_150497_c() * 0.15F, soundType.func_150494_d());
		}
	}
}
