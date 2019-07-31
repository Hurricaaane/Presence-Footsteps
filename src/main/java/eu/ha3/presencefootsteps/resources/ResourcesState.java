package eu.ha3.presencefootsteps.resources;

public enum ResourcesState {
    NONE,
    LOADED,
    UNLOADED;

    public boolean isFunctional() {
        return this == LOADED;
    }
}
