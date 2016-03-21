package eu.ha3.presencefootsteps;

import eu.ha3.presencefootsteps.util.MineLittlePonyCommunicator;

public enum Stance {
	QUAD,
	BIPED,
	QUAD_PEG;
	
	
	public boolean isMLP() {
		return this != BIPED;
	}
	
	public boolean isPeg() {
		return this == QUAD_PEG;
	}
	
	public static Stance getStance(int stanceId) {
		if (stanceId < 0 || stanceId > values().length) stanceId = 0;
		if (stanceId == 0) {
			int race = MineLittlePonyCommunicator.instance.getEffectiveRace();
			if (race > 0) {
				if (race == 2) return QUAD_PEG;
				return QUAD;
			}
			return BIPED;
		}
		
		return values()[stanceId - 1];
	}
}
