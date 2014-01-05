package eu.ha3.mc.presencefootsteps.parsers;

import java.util.Map;
import java.util.Map.Entry;

import eu.ha3.mc.presencefootsteps.game.system.PFHaddon;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.BlockMap;
import eu.ha3.util.property.contract.PropertyHolder;

/* x-placeholder-wtfplv2 */

public class PropertyBlockMap_Engine0
{
	public void setup(PropertyHolder blockSound, BlockMap blockMap)
	{
		Map<String, String> properties = blockSound.getAllProperties();
		for (Entry<String, String> entry : properties.entrySet())
		{
			try
			{
				blockMap.register(entry.getKey(), entry.getValue());
			}
			catch (Exception e)
			{
				PFHaddon.log("Error when registering block " + entry.getKey() + ": " + e.getMessage());
			}
		}
	}
}
