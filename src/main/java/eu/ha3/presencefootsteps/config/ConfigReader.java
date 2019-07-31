package eu.ha3.presencefootsteps.config;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigReader implements Closeable {

    private final BufferedReader reader;

    public ConfigReader(InputStream stream) {
        reader = new BufferedReader(new InputStreamReader(stream));
    }

    public Stream<Property> properties() {
        return reader.lines().map(PropertyImpl::new);
    }

    public Map<String, Property> sheet() {
        return properties().collect(Collectors.toMap(Property::getName, p -> p));
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private class PropertyImpl implements Property {

        private final String key;

        private String value;

        PropertyImpl(String line) {
            line = line.trim();

            String key = line.split("=")[0];

            value = line.replace(key + "=", "").trim();
            this.key = key.trim().toLowerCase();
        }

        @Override
        public String getString() {
            return value;
        }

        @Override
        public String getName() {
            return key;
        }

        @Override
        public <T> void set(T value) {
            this.value = Objects.toString(value);
        }
    }
}
