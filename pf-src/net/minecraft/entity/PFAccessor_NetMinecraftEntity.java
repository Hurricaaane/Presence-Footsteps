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
	
	@Deprecated
	public boolean isJumping(EntityLivingBase entityLiving)
	{
		return entityLiving.isJumping;
	}
	
	@Override
	public void playStep(EntityLivingBase entity, int xx, int yy, int zz, Block blockID)
	{
		//playStepSound
		//entity.func_145780_a(xx, yy, zz, blockID);
		
		Block.SoundType soundType = blockID.field_149762_H;
		
		if (Minecraft.getMinecraft().theWorld.func_147439_a(xx, yy + 1, zz) == Blocks.field_150431_aC)
		{
			soundType = Blocks.field_150431_aC.field_149762_H;
			entity.playSound(soundType.func_150498_e(), soundType.func_150497_c() * 0.15F, soundType.func_150494_d());
		}
		else if (!blockID.func_149688_o().isLiquid())
		{
			entity.playSound(soundType.func_150498_e(), soundType.func_150497_c() * 0.15F, soundType.func_150494_d());
		}
	}
}
