package eu.ha3.mc.presencefootsteps.mcpackage.implem;

import java.util.LinkedHashMap;
import java.util.Map;

import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.BlockMap;

/* x-placeholder-wtfplv2 */

public class BasicBlockMap implements BlockMap
{
	private Map<String, String> blockMap;
	
	public BasicBlockMap()
	{
		this.blockMap = new LinkedHashMap<String, String>();
	}
	
	@Override
	public String getBlockMap(String blockName, int meta)
	{
		String material = null;
		
		if (this.blockMap.containsKey(blockName + "_" + meta))
		{
			material = this.blockMap.get(blockName + "_" + meta);
		}
		else if (this.blockMap.containsKey(blockName))
		{
			material = this.blockMap.get(blockName);
		}
		else
		{
			material = null;
		}
		
		return material;
	}
	
	@Override
	public String getBlockMapSubstrate(String blockName, int meta, String substrate)
	{
		String material = null;
		
		if (this.blockMap.containsKey(blockName + "_" + meta + "." + substrate))
		{
			material = this.blockMap.get(blockName + "_" + meta + "." + substrate);
		}
		else if (this.blockMap.containsKey(blockName + "." + substrate))
		{
			material = this.blockMap.get(blockName + "." + substrate);
		}
		else
			return null;
		
		return material;
	}
	
	@Override
	public void register(String key, String value)
	{
		this.blockMap.put(key.replace('>', ':'), value);
	}
}
