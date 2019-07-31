package eu.ha3.presencefootsteps.sound;

public enum ResourcesState {
    NONE,
    LOADED,
    UNLOADED;

    public boolean isFunctional() {
        return this == LOADED;
    }
}
