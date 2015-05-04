package eu.ha3.mc.presencefootsteps.engine.interfaces;

public enum EventType {
	WALK(null),//		final form
	WANDER(null),//		final form
	SWIM(null),//		final form
	RUN(WALK), //		-> walk
	JUMP(WANDER),// 	-> wander
	LAND(RUN),//		-> run
	CLIMB(WALK),//		-> walk
	CLIMB_RUN(RUN),//	-> run
	DOWN(WALK),//		-> walk
	DOWN_RUN(RUN),//	-> run
	UP(WALK),//			-> walk
	UP_RUN(RUN);//		-> run
	
	private final EventType destination;
	private final String jsonName;
	
	EventType(EventType dest) {
		destination = dest;
		jsonName = name().toLowerCase();
	}
	
	public String jsonName() {
		return jsonName;
	}
	
	public boolean canTransition() {
		return destination != null;
	}
	
	public EventType getTransitionDestination() {
		return destination;
	}
}
