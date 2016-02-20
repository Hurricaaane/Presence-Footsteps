package eu.ha3.presencefootsteps.game.interfaces;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

/* x-placeholder-wtfplv2 */

public interface BlockMap {
	
	/**
	 * This will return null if the block is not defined, and NOT_EMITTER if the block is a non-emitting block,
	 * meaning block resolution must continue on its neighbours.
	 */
	public String getBlockMap(IBlockState state);
	
	/**
	 * This will return null if the substrate does not resolve in the selected carpet.
	 */
	public String getBlockMapSubstrate(IBlockState state, String substrate);
	
	/**
	 * Register a blockmap entry.
	 */
	public void register(String key, String value);
	
	/**
	 * Checks if a blockmap entry exists for the given block.
	 */
	public boolean hasEntryForBlock(Block block);
	
}