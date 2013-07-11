package eu.ha3.mc.presencefootsteps.engine.implem;

import java.util.HashMap;
import java.util.Map;

import eu.ha3.mc.presencefootsteps.engine.interfaces.Acoustic;
import eu.ha3.mc.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.mc.presencefootsteps.engine.interfaces.NamedAcoustic;
import eu.ha3.mc.presencefootsteps.engine.interfaces.Options;
import eu.ha3.mc.presencefootsteps.engine.interfaces.SoundPlayer;

/*
            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
                    Version 2, December 2004 

 Copyright (C) 2004 Sam Hocevar <sam@hocevar.net> 

 Everyone is permitted to copy and distribute verbatim or modified 
 copies of this license document, and changing it is allowed as long 
 as the name is changed. 

            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION 

  0. You just DO WHAT THE FUCK YOU WANT TO. 
*/

public class EventSelectorAcoustics implements NamedAcoustic
{
	private final String name;
	
	private Map<EventType, Acoustic> pairs;
	
	private static Map<EventType, EventType> fallback;
	static
	{
		fallback = new HashMap<EventType, EventType>();
		fallback.put(EventType.RUN, EventType.WALK);
		fallback.put(EventType.LAND, EventType.RUN);
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
