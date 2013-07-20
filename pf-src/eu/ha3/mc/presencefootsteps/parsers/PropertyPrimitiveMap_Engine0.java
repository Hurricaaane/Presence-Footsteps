package eu.ha3.mc.presencefootsteps.parsers;

import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.src.PFHaddon;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.PrimitiveMap;
import eu.ha3.util.property.contract.PropertyHolder;

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

public class PropertyPrimitiveMap_Engine0
{
	public void setup(PropertyHolder primitiveSound, PrimitiveMap primitiveMap)
	{
		Map<String, String> properties = primitiveSound.getAllProperties();
		for (Entry<String, String> entry : properties.entrySet())
		{
			try
			{
				primitiveMap.register(entry.getKey(), entry.getValue());
			}
			catch (Exception e)
			{
				PFHaddon.log("Error when registering primitive " + entry.getKey() + ": " + e.getMessage());
			}
		}
	}
}
