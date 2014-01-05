package eu.ha3.mc.presencefootsteps.parsers;

import java.util.Map;
import java.util.Map.Entry;

import eu.ha3.mc.presencefootsteps.game.system.PFHaddon;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.PrimitiveMap;
import eu.ha3.util.property.contract.PropertyHolder;

/* x-placeholder-wtfplv2 */

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
