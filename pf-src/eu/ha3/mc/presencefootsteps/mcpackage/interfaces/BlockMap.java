package eu.ha3.mc.presencefootsteps.mcpackage.interfaces;

/*
            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
                    Version 2, December 2004 

 Copyright (C) 2004 Sam Hocevar <sam@hocevar.net> 

 Everyone is permitted to copy and distribute verbatim or modified 
 copies of this license document, and changing it is allowed as long 
 as the name is changed. 

            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION 

  0. You just DO WHAT THE FUCK YOU WANT TO. 
*/

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
	public abstract String getBlockMap(int block, int meta);
	
	/**
	 * This will return null if the block is not a carpet.
	 * 
	 * @param carpet
	 * @param meta
	 * @param event
	 * @return
	 */
	public abstract String getBlockMapForCarpet(int carpet, int meta);
	
	/**
	 * Register an blockmap entry.
	 * 
	 * @param key
	 * @param value
	 */
	public abstract void register(String key, String value);
	
}