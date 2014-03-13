package eu.ha3.mc.presencefootsteps.game.system;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

/*
--filenotes-placeholder
*/

public class PF172Helper
{
	/**
	 * Gets the block at a certain location in the current world. This method is
	 * not safe against locations in undefined space.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static Block getBlockAt(int x, int y, int z)
	{
		return getBlockAt(Minecraft.getMinecraft().theWorld, x, y, z);
	}
	
	/**
	 * Gets the name of the block at a certain location in the current world. If
	 * the location is in an undefined space (lower than zero or higher than the
	 * current world getHeight(), or throws any exception during evaluation), it
	 * will return a default string.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param defaultIfFail
	 * @return
	 */
	public static String getNameAt(int x, int y, int z, String defaultIfFail)
	{
		if (y <= 0 || y >= Minecraft.getMinecraft().theWorld.getHeight())
			return defaultIfFail;
		
		try
		{
			return getNameAt(Minecraft.getMinecraft().theWorld, x, y, z);
		}
		catch (Exception e)
		{
			return defaultIfFail;
		}
	}
	
	/**
	 * Gets the block at a certain location in the given world. This method is
	 * not safe against locations in undefined space.
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private static Block getBlockAt(World world, int x, int y, int z)
	{
		Block block = world.getBlock(x, y, z);
		return block;
	}
	
	/**
	 * Gets the name of the block at a certain location in the given world. This
	 * method is not safe against locations in undefined space.
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private static String getNameAt(World world, int x, int y, int z)
	{
		return nameOf(getBlockAt(world, x, y, z));
	}
	
	//
	
	/**
	 * Gets the unique name of a given block, defined by its interoperability
	 * identifier.
	 * 
	 * @param block
	 * @return
	 */
	public static String nameOf(Block block)
	{
		// RegistryNamespaced
		return Block.blockRegistry.getNameForObject(block);
	}
}