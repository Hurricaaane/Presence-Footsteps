package eu.ha3.presencefootsteps.util;

import eu.ha3.mc.haddon.Utility;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class PFHelper {
	/**
	 * Gets the block at a certain location in the current world. This method is not safe against locations in undefined space.
	 */
	public static Block getBlockAt(BlockPos pos) {
		return getBlockAt(Minecraft.getMinecraft().theWorld, pos);
	}
	
	/**
	 * Gets the name of the block at a certain location in the current world.
	 * If the location is in an undefined space (lower than zero or higher than the current world getHeight(),
	 * or throws any exception during evaluation), it will return a default string.
	 */
	public static String getNameAt(BlockPos pos, String defaultIfFail) {
		if (pos.getY() > 0 && pos.getY() < Minecraft.getMinecraft().theWorld.getHeight()) {
			try {
				return getNameAt(Minecraft.getMinecraft().theWorld, pos);
			} catch (Exception e) {}
		}
		return defaultIfFail;
	}
	
	/**
	 * Gets the block at a certain location in the given world.
	 * This method is not safe against locations in undefined space.
	 */
	private static Block getBlockAt(World world, BlockPos pos) {
		return world.getBlockState(pos).getBlock();
	}
	
	/**
	 * Gets the name of the block at a certain location in the given world.
	 * This method is not safe against locations in undefined space.
	 */
	private static String getNameAt(World world, BlockPos pos) {
		return nameOf(getBlockAt(world, pos));
	}
	
	/**
	 * Gets the unique name of a given block, defined by its resource location identifier.
	 */
	public static String nameOf(Block block) {
		return ((ResourceLocation)Block.blockRegistry.getNameForObject(block)).toString(); // RegistryNamespaced
	}
	
	/**
	 * Checks if the game is paused.
	 * <p>
	 * i.e There are no guis currently open that pause the game, we are not on a lan server, nor on a dedicated server. 
	 */
	public static boolean isGamePaused(Utility util) {
		Object current = util.getCurrentScreen();
		Minecraft mc = Minecraft.getMinecraft();
		return current != null && (((GuiScreen)current).doesGuiPauseGame() && mc.isSingleplayer() && !mc.getIntegratedServer().getPublic());
	}
}