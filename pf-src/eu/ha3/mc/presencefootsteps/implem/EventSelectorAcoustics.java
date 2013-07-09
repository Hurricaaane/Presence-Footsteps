package eu.ha3.mc.presencefootsteps.implem;

import java.util.HashMap;
import java.util.Map;

import eu.ha3.mc.presencefootsteps.interfaces.Acoustic;
import eu.ha3.mc.presencefootsteps.interfaces.EventType;
import eu.ha3.mc.presencefootsteps.interfaces.NamedAcoustic;
import eu.ha3.mc.presencefootsteps.interfaces.SoundPlayer;

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
	public void playSound(SoundPlayer player, double x, double y, double z, EventType event)
	{
		if (this.pairs.containsKey(event))
		{
			this.pairs.get(event).playSound(player, x, y, z, event);
		}
	}
	
	public void setAcousticPair(EventType type, Acoustic acoustic)
	{
		this.pairs.put(type, acoustic);
	}
	
}
