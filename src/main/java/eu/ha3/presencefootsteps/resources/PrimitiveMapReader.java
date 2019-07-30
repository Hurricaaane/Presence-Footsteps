package eu.ha3.presencefootsteps.resources;

import java.util.Map;
import java.util.Map.Entry;

import eu.ha3.presencefootsteps.game.interfaces.PrimitiveMap;
import eu.ha3.presencefootsteps.log.PFLog;
import eu.ha3.util.property.contract.PropertyHolder;

/* x-placeholder-wtfplv2 */

public class PrimitiveMapReader {
	public void setup(PropertyHolder primitiveSound, PrimitiveMap primitiveMap) {
		Map<String, String> properties = primitiveSound.getAllProperties();
		for (Entry<String, String> entry : properties.entrySet()) {
			try {
				primitiveMap.register(entry.getKey(), entry.getValue());
			} catch (Exception e) {
				PFLog.log("Error when registering primitive " + entry.getKey() + ": " + e.getMessage());
			}
		}
	}
}
