package eu.ha3.mc.presencefootsteps.implem;

import eu.ha3.mc.presencefootsteps.interfaces.Options;

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

public class DelayedAcoustic extends BasicAcoustic implements Options
{
	protected float delay;
	
	public DelayedAcoustic()
	{
		super();
		
		this.options = this;
	}
	
	@Override
	public boolean hasOption(String option)
	{
		return option.equals("delay");
	}
	
	@Override
	public Object getOption(String option)
	{
		return this.delay;
	}
	
	//
	
	public void setDelay(float delay)
	{
		this.delay = delay;
	}
}
