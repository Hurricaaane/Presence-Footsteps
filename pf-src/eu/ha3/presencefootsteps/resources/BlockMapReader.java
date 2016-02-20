package eu.ha3.presencefootsteps.resources;

import java.util.Map;
import java.util.Map.Entry;

import eu.ha3.presencefootsteps.game.interfaces.BlockMap;
import eu.ha3.presencefootsteps.log.PFLog;
import eu.ha3.util.property.contract.PropertyHolder;

public class BlockMapReader {
	public void setup(PropertyHolder blockSound, BlockMap blockMap) {
		Map<String, String> properties = blockSound.getAllProperties();
		for (Entry<String, String> entry : properties.entrySet()) {
			try {
				blockMap.register(entry.getKey(), entry.getValue());
			} catch (Exception e) {
				PFLog.log("Error when registering block " + entry.getKey() + ": " + e.getMessage());
			}
		}
	}
}
