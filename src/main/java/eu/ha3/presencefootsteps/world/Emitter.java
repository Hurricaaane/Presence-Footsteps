package eu.ha3.presencefootsteps.world;

public final class Emitter {
    private Emitter() {}

    public static final String UNASSIGNED = "UNASSIGNED";
    public static final String NOT_EMITTER = "NOT_EMITTER";

    public static boolean isNonEmitter(String association) {
        return NOT_EMITTER.equals(association);
    }

    public static boolean isResult(String association) {
        return !UNASSIGNED.equals(association);
    }

    public static boolean isEmitter(String association) {
        return isResult(association) && !isNonEmitter(association);
    }

}
