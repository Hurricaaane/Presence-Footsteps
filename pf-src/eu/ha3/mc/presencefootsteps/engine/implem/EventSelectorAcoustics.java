package eu.ha3.mc.presencefootsteps.engine.implem;

import java.util.HashMap;
import java.util.Map;

import eu.ha3.mc.presencefootsteps.engine.interfaces.Acoustic;
import eu.ha3.mc.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.mc.presencefootsteps.engine.interfaces.NamedAcoustic;
import eu.ha3.mc.presencefootsteps.engine.interfaces.Options;
import eu.ha3.mc.presencefootsteps.engine.interfaces.SoundPlayer;

public class EventSelectorAcoustics implements NamedAcoustic {
	private final String name;
	
	private Map<EventType, Acoustic> pairs;
	
	public EventSelectorAcoustics(String name) {
		this.name = name;
		pairs = new HashMap<EventType, Acoustic>();
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void playSound(SoundPlayer player, Object location, EventType event, Options inputOptions) {
		if (pairs.containsKey(event)) {
			pairs.get(event).playSound(player, location, event, inputOptions);
		} else if (event.canTransition()) {
			playSound(player, location, event.getTransitionDestination(), inputOptions); // the possibility of a resonance cascade scenario is extremely unlikely
		}
	}
	
	public void setAcousticPair(EventType type, Acoustic acoustic) {
		pairs.put(type, acoustic);
	}
	
}
