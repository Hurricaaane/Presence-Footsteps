package eu.ha3.presencefootsteps.sound;

import java.util.HashMap;

public interface Options {

    static Options create() {
        return new MapOptions();
    }

    boolean containsKey(Object option);

    <T> T get(String option);

    <T> Options withOption(String option, T value);

    final class MapOptions extends HashMap<String, Object> implements Options {
        private static final long serialVersionUID = 1L;

        @SuppressWarnings("unchecked")
        @Override
        public <T> T get(String option) {
            return (T)super.get(option);
        }

        @Override
        public MapOptions withOption(String option, Object value) {
            put(option, value);
            return this;
        }
    }
}
