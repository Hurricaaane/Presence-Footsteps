package eu.ha3.mc.presencefootsteps.mcpackage.implem;

import java.util.LinkedHashMap;
import java.util.Map;

import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.PrimitiveMap;

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
