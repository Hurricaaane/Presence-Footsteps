package net.minecraft.src;

import java.util.LinkedHashMap;
import java.util.Map;

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

public class PFBlockMap
{
	private Map<String, String> blockMap;
	
	public PFBlockMap()
	{
		this.blockMap = new LinkedHashMap<String, String>();
	}
	
	/**
	 * This will return null if the block is not defined, and NOT_EMITTER if the
	 * block is a non-emitting block, meaning block resolution must continue on
	 * its neighbors.
	 * 
	 * @param block
	 * @param meta
	 * @return
	 */
	public String getBlockMap(int block, int meta)
	{
		String material = null;
		
		if (this.blockMap.containsKey(block + "_" + meta))
		{
			material = this.blockMap.get(block + "_" + meta);
		}
		else if (this.blockMap.containsKey(Integer.toString(block)))
		{
			material = this.blockMap.get(Integer.toString(block));
		}
		else
		{
			material = null;
		}
		
		return material;
	}
	
	/**
	 * This will return null if the block is not a carpet.
	 * 
	 * @param carpet
	 * @param meta
	 * @param event
	 * @return
	 */
	public String getBlockMapForCarpet(int carpet, int meta)
	{
		String material = null;
		
		if (this.blockMap.containsKey(carpet + "_" + meta + ".carpet"))
		{
			material = this.blockMap.get(carpet + "_" + meta + ".carpet");
		}
		else if (this.blockMap.containsKey(Integer.toString(carpet) + ".carpet"))
		{
			material = this.blockMap.get(Integer.toString(carpet) + ".carpet");
		}
		else
			return null;
		
		return material;
	}
	
	/**
	 * Register an blockmap entry.
	 * 
	 * @param key
	 * @param value
	 */
	public void register(String key, String value)
	{
		this.blockMap.put(key, value);
	}
}
