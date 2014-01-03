package eu.ha3.mc.presencefootsteps.mcpackage.implem;

import java.util.LinkedHashMap;
import java.util.Map;

import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.PrimitiveMap;

/* x-placeholder-wtfplv2 */

public class BasicPrimitiveMap implements PrimitiveMap
{
	private Map<String, String> primitiveMap;
	
	public BasicPrimitiveMap()
	{
		this.primitiveMap = new LinkedHashMap<String, String>();
	}
	
	@Override
	public String getPrimitiveMap(String primitive)
	{
		String material = null;
		
		if (this.primitiveMap.containsKey(primitive))
		{
			material = this.primitiveMap.get(primitive);
		}
		else
		{
			material = null;
		}
		
		return material;
	}
	
	@Override
	public String getPrimitiveMapSubstrate(String primitive, String substrate)
	{
		String material = null;
		
		if (this.primitiveMap.containsKey(primitive + "@" + substrate))
		{
			material = this.primitiveMap.get(primitive + "@" + substrate);
		}
		else
		{
			material = null;
		}
		
		return material;
	}
	
	@Override
	public void register(String key, String value)
	{
		this.primitiveMap.put(key, value);
	}
}
