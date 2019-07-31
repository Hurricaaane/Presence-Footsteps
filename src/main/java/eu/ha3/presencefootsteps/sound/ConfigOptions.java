package eu.ha3.presencefootsteps.sound;

import java.util.HashMap;
import java.util.Map;

public class ConfigOptions implements Options {

    private final Map<String, Object> map = new HashMap<>();

    public Map<String, Object> getMap() {
        return map;
    }

    @Override
    public boolean containsKey(String option) {
        return map.containsKey(option);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String option) {
        return (T)map.get(option);
    }

    @Override
    public ConfigOptions withOption(String option, Object value) {
        map.put(option, value);
        return this;
    }
}
