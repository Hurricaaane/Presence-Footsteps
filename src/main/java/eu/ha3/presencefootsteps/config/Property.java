package eu.ha3.presencefootsteps.config;

public interface Property {

    String getName();

    String getString();

    <T> void set(T value);

    default int getInteger() {
        return Integer.valueOf(getString());
    }

    default int getByte() {
        return Byte.valueOf(getString());
    }

    default long getLong() {
        return Long.valueOf(getString());
    }

    default float getFloat() {
        return Float.valueOf(getString());
    }

    default double getDouble() {
        return Double.valueOf(getString());
    }

    default boolean getBoolean() {
        return Boolean.valueOf(getString());
    }
}
