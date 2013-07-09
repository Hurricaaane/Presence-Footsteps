package eu.ha3.mc.presencefootsteps.engine.implem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import eu.ha3.mc.presencefootsteps.engine.interfaces.Acoustic;
import eu.ha3.mc.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.mc.presencefootsteps.engine.interfaces.Library;
import eu.ha3.mc.presencefootsteps.engine.interfaces.NamedAcoustic;
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

public abstract class AcousticsLibrary implements Library
{
	private Map<String, Acoustic> acoustics;
	
	protected SoundPlayer myPlayer;
	
	public AcousticsLibrary()
	{
		this.acoustics = new HashMap<String, Acoustic>();
	}
	
	@Override
	public void addAcoustic(NamedAcoustic acoustic)
	{
		this.acoustics.put(acoustic.getName(), acoustic);
	}
	
	@Override
	public Set<String> getAcousticsKeySet()
	{
		return this.acoustics.keySet();
	}
	
	@Override
	public Acoustic getAcoustic(String acoustic)
	{
		if (!this.acoustics.containsKey(acoustic))
			return null;
		
		return this.acoustics.get(acoustic);
	}
	
	@Override
	public void playAcoustic(Object location, String acousticName, EventType event)
	{
		if (!this.acoustics.containsKey(acousticName))
		{
			onMaterialNotFound();
			return;
		}
		
		this.acoustics.get(acousticName).playSound(this.myPlayer, location, event);
	}
	
	@Override
	public boolean hasAcoustic(String acousticName)
	{
		return this.acoustics.containsKey(acousticName);
	}
	
	protected abstract void onMaterialNotFound();
}
