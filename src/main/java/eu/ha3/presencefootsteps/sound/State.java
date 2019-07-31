package eu.ha3.presencefootsteps.sound;

public enum State {
    STAND(null), WALK(null), WANDER(null), SWIM(null), RUN(WALK), JUMP(WANDER), LAND(RUN), CLIMB(WALK), CLIMB_RUN(RUN),
    DOWN(WALK), DOWN_RUN(RUN), UP(WALK), UP_RUN(RUN);

    private final State destination;

    private final String jsonName;

    State(State dest) {
        destination = dest;
        jsonName = name().toLowerCase();
    }

    public String getName() {
        return jsonName;
    }

    public boolean canTransition() {
        return destination != null;
    }

    public State getTransitionDestination() {
        return destination;
    }
}
