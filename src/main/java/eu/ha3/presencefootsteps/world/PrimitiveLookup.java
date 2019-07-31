package eu.ha3.presencefootsteps.world;

import java.util.LinkedHashMap;
import java.util.Map;

public class PrimitiveLookup implements Lookup<String> {

    private final Map<String, String> primitiveMap = new LinkedHashMap<>();

    @Override
    public String getAssociation(String primitive) {
        return primitiveMap.get(primitive);
    }

    @Override
    public String getAssociation(String primitive, String substrate) {
        return getAssociation(primitive + "@" + substrate);
    }

    @Override
    public void add(String key, String value) {
        primitiveMap.put(key, value);
    }

    @Override
    public boolean contains(String key) {
        return primitiveMap.containsKey(key);
    }
}
