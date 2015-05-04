package eu.ha3.mc.presencefootsteps.mcpackage.implem;

import java.util.LinkedHashMap;
import java.util.Map;

import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.PrimitiveMap;

public class BasicPrimitiveMap implements PrimitiveMap {
	private Map<String, String> primitiveMap;
	
	public BasicPrimitiveMap() {
		primitiveMap = new LinkedHashMap<String, String>();
	}
	
	@Override
	public String getPrimitiveMap(String primitive) {
		if (primitiveMap.containsKey(primitive)) {
			return primitiveMap.get(primitive);
		}
		return null;
	}
	
	@Override
	public String getPrimitiveMapSubstrate(String primitive, String substrate) {
		if (primitiveMap.containsKey(primitive + "@" + substrate)) {
			return primitiveMap.get(primitive + "@" + substrate);
		}
		return null;
	}
	
	@Override
	public void register(String key, String value) {
		primitiveMap.put(key, value);
	}
}
