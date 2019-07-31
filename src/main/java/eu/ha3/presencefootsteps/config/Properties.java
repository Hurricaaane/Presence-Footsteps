package eu.ha3.presencefootsteps.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Properties {

    private final Map<String, Property> values = new HashMap<>();

    public Property getProperty(String key) {
        return values.computeIfAbsent(key, PropertyImpl::new);
    }

    public <T> void setProperty(String key, T value) {
        getProperty(key).set(value);
    }

    public void load(Path from) throws IOException {
        if (Files.isReadable(from)) {
            try (ConfigReader input = new ConfigReader(Files.newInputStream(from))) {
                input.properties().forEach(property -> {
                    values.put(property.getName(), property);
                });
            }
        }

        save(from);
    }

    public void save(Path to) throws IOException {

        Files.deleteIfExists(to);
        Files.createFile(to);

        try (ConfigWriter output = new ConfigWriter(Files.newOutputStream(to))) {
            values.values().forEach(property -> {
                try {
                    output.writeProperty(property);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private class PropertyImpl implements Property {

        private final String name;

        public String value;

        PropertyImpl(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getString() {
            return value;
        }

        @Override
        public <T> void set(T value) {
            this.value = Objects.toString(value);
        }
    }
}
