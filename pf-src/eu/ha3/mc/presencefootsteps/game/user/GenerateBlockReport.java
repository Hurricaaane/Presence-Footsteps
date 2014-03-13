package eu.ha3.mc.presencefootsteps.game.user;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import eu.ha3.mc.presencefootsteps.game.system.PF172Helper;
import eu.ha3.util.property.simple.ConfigProperty;

/*
--filenotes-placeholder
*/

public class GenerateBlockReport
{
	private ConfigProperty results;
	
	public GenerateBlockReport()
	{
		this.results = new ConfigProperty();
		
		for (Object o : Block.blockRegistry)
		{
			Block block = (Block) o;
			String name = PF172Helper.nameOf(block);
			
			// stepSound.stepSoundName
			String soundName;
			if (block.stepSound == null)
			{
				soundName = "NO_STEP";
			}
			else if (block.stepSound.field_150501_a == null)
			{
				soundName = "NO_SOUND";
			}
			else
			{
				soundName = block.stepSound.field_150501_a;
			}
			
			if (block instanceof BlockLiquid)
			{
				soundName += "," + "EXTENDS_LIQUID";
			}
			if (block instanceof BlockBush)
			{
				soundName += "," + "EXTENDS_BUSH";
			}
			if (block instanceof BlockDoublePlant)
			{
				soundName += "," + "EXTENDS_DOUBLE_PLANT";
			}
			if (block instanceof BlockCrops)
			{
				soundName += "," + "EXTENDS_CROPS";
			}
			if (block instanceof BlockContainer)
			{
				soundName += "," + "EXTENDS_CONTAINER";
			}
			if (block instanceof BlockLeavesBase)
			{
				soundName += "," + "EXTENDS_LEAVES";
			}
			if (block instanceof BlockRailBase)
			{
				soundName += "," + "EXTENDS_RAIL";
			}
			if (block instanceof BlockSlab)
			{
				soundName += "," + "EXTENDS_SLAB";
			}
			if (block instanceof BlockStairs)
			{
				soundName += "," + "EXTENDS_STAIRS";
			}
			if (block instanceof BlockBreakable)
			{
				soundName += "," + "EXTENDS_BREAKABLE";
			}
			if (block instanceof BlockFalling)
			{
				soundName += "," + "EXTENDS_PHYSICALLY_FALLING";
			}
			if (block instanceof BlockPane)
			{
				soundName += "," + "EXTENDS_PANE";
			}
			if (block instanceof BlockRotatedPillar)
			{
				soundName += "," + "EXTENDS_PILLAR";
			}
			if (block instanceof BlockTorch)
			{
				soundName += "," + "EXTENDS_TORCH";
			}
			/*if (!block.func_149662_c())
			{
				soundName += "," + "FUNC_POPPABLE";
			}*/
			if (!block.isOpaqueCube())
			{
				soundName += "," + "HITBOX";
			}
			
			this.results.setProperty(name, soundName);
		}
		this.results.commit();
	}
	
	public ConfigProperty getResults()
	{
		return this.results;
	}
}
