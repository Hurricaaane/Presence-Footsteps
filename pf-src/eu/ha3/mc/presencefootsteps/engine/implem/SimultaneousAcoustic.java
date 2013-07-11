package eu.ha3.mc.presencefootsteps.engine.implem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.ha3.mc.presencefootsteps.engine.interfaces.Acoustic;
import eu.ha3.mc.presencefootsteps.engine.interfaces.EventType;
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

public class SimultaneousAcoustic implements Acoustic
{
	protected List<Acoustic> acoustics;
	
	public SimultaneousAcoustic(Collection<Acoustic> acoustics)
	{
		this.acoustics = new ArrayList<Acoustic>(acoustics);
	}
	
	@Override
	public void playSound(SoundPlayer player, Object location, EventType event, Options inputOptions)
	{
		for (Acoustic acoustic : this.acoustics)
		{
			acoustic.playSound(player, location, event, inputOptions);
		}
		
	}
	
}
