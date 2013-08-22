package net.minecraft.src;

import eu.ha3.mc.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Isolator;

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

public class PFReaderQP extends PFReaderH
{
	private int hoof = 0;
	
	public PFReaderQP(Isolator isolator)
	{
		super(isolator);
	}
	
	@Override
	protected void stepped(EntityPlayer ply, EventType event)
	{
		if (this.hoof >= 3)
		{
			this.hoof = 0;
		}
		else
		{
			this.hoof = this.hoof + 1;
		}
		
		if (this.hoof == 3 && event == EventType.RUN)
		{
			produceStep(ply, event);
			this.hoof = 0;
		}
		
		if (event == EventType.WALK)
		{
			produceStep(ply, event);
		}
	}
	
	@Override
	protected float reevaluateDistance(EventType event, float distance)
	{
		float ret = distance;
		
		//if (event == EventType.WALK && (this.hoof == 1 || this.hoof == 3))
		//	return ret * 0.01f;
		
		if (event == EventType.RUN && this.hoof == 0)
			return ret * 0.8f;
		
		if (event == EventType.RUN)
			return ret * 0.3f;
		
		return ret;
	}
	
}
