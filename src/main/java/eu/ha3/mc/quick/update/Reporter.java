package eu.ha3.mc.quick.update;

@FunctionalInterface
public interface Reporter {
    void report(TargettedVersion newVersion, TargettedVersion currentVersion);
}