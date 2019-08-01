package eu.ha3.presencefootsteps.sound;

import java.util.HashMap;

public interface Options {

    Options EMPTY = new Options() {
        @Override
        public boolean containsKey(Object option) {
            return false;
        }

        @Override
        public <T> T get(String option) {
            return null;
        }
    };

    static Options singular(String key, Object value) {
        MapOptions options = new MapOptions();
        options.put(key, value);
        return options;
    }

    boolean containsKey(Object option);

    <T> T get(String option);

    final class MapOptions extends HashMap<String, Object> implements Options {
        private static final long serialVersionUID = 1L;

        @SuppressWarnings("unchecked")
        @Override
        public <T> T get(String option) {
            return (T)super.get(option);
        }
    }
}
