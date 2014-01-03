package eu.ha3.mc.presencefootsteps.engine.implem;

import java.util.HashMap;
import java.util.Map;

import eu.ha3.mc.presencefootsteps.engine.interfaces.Acoustic;
import eu.ha3.mc.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.mc.presencefootsteps.engine.interfaces.NamedAcoustic;
import eu.ha3.mc.presencefootsteps.engine.interfaces.Options;
import eu.ha3.mc.presencefootsteps.engine.interfaces.SoundPlayer;

/* x-placeholder-wtfplv2 */

public class EventSelectorAcoustics implements NamedAcoustic
{
	private final String name;
	
	private Map<EventType, Acoustic> pairs;
	
	private static Map<EventType, EventType> fallback;
	static
	{
		fallback = new HashMap<EventType, EventType>();
		fallback.put(EventType.LAND, EventType.RUN);
		fallback.put(EventType.RUN, EventType.WALK);
		fallback.put(EventType.JUMP, EventType.WANDER);
		
		fallback.put(EventType.CLIMB, EventType.WANDER);
		fallback.put(EventType.DOWN, EventType.WALK);
		fallback.put(EventType.UP, EventType.WALK);
		
		fallback.put(EventType.CLIMB_RUN, EventType.RUN);
		fallback.put(EventType.DOWN_RUN, EventType.RUN);
		fallback.put(EventType.UP_RUN, EventType.RUN);
	}
	
	public EventSelectorAcoustics(String name)
	{
		this.name = name;
		this.pairs = new HashMap<EventType, Acoustic>();
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public void playSound(SoundPlayer player, Object location, EventType event, Options inputOptions)
	{
		if (this.pairs.containsKey(event))
		{
			this.pairs.get(event).playSound(player, location, event, inputOptions);
		}
		else if (fallback.containsKey(event))
		{
			EventType substituteEvent = fallback.get(event);
			
			// the possibility of a resonance cascade scenario is extremely unlikely
			playSound(player, location, substituteEvent, inputOptions);
		}
	}
	
	public void setAcousticPair(EventType type, Acoustic acoustic)
	{
		this.pairs.put(type, acoustic);
	}
	
}
