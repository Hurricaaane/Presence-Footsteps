package eu.ha3.presencefootsteps.config;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Objects;

public class ConfigWriter implements Closeable {

    private static final String FORMAT = "%s\t\t = %s\n";

    private final BufferedWriter writer;

    public ConfigWriter(OutputStream stream) {
        writer = new BufferedWriter(new OutputStreamWriter(stream));
    }

    public void writeProperty(Property property) throws IOException {
        writeProperty(property.getName(), property.getString());
    }

    public void writeProperty(String key, Object value) throws IOException {
        writer.write(String.format(FORMAT, key, Objects.toString(value)));
    }

    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }
}
