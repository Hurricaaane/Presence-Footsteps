package eu.ha3.mc.presencefootsteps.mcpackage.interfaces;

/* x-placeholder-wtfplv2 */

public interface BlockMap
{
	
	/**
	 * This will return null if the block is not defined, and NOT_EMITTER if the
	 * block is a non-emitting block, meaning block resolution must continue on
	 * its neighbors.
	 * 
	 * @param block
	 * @param meta
	 * @return
	 */
	public abstract String getBlockMap(String blockName, int meta);
	
	/**
	 * This will return null if the substrate does not resolve in the selected
	 * carpet.
	 * 
	 * @param carpet
	 * @param meta
	 * @param event
	 * @return
	 */
	public abstract String getBlockMapSubstrate(String blockName, int meta, String substrate);
	
	/**
	 * Register an blockmap entry.
	 * 
	 * @param key
	 * @param value
	 */
	public abstract void register(String key, String value);
	
}