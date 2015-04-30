package eu.ha3.mc.presencefootsteps.mcpackage.interfaces;

import net.minecraft.block.state.IBlockState;

/* x-placeholder-wtfplv2 */

public interface BlockMap
{
	
	/**
	 * This will return null if the block is not defined, and NOT_EMITTER if the
	 * block is a non-emitting block, meaning block resolution must continue on
	 * its neighbors.
	 * 
	 * @param blockName
	 * @param meta
	 * @return
	 */
	public abstract String getBlockMap(IBlockState state);
	
	/**
	 * This will return null if the substrate does not resolve in the selected
	 * carpet.
	 * 
	 * @param blockName
	 * @param meta
	 * @param substrate
	 * @return
	 */
	public abstract String getBlockMapSubstrate(IBlockState state, String substrate);
	
	/**
	 * Register an blockmap entry.
	 * 
	 * @param key
	 * @param value
	 */
	public abstract void register(String key, String value);
	
}