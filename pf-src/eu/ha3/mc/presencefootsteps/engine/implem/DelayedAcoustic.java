package eu.ha3.mc.presencefootsteps.engine.implem;

import eu.ha3.mc.presencefootsteps.engine.interfaces.Options;

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
	protected long delayMin = 0;
	protected long delayMax = 0;
	
	public DelayedAcoustic()
	{
		super();
		
		this.outputOptions = this;
	}
	
	@Override
	public boolean hasOption(String option)
	{
		return option.equals("delay_min") || option.equals("delay_max");
	}
	
	@Override
	public Object getOption(String option)
	{
		return option.equals("delay_min") ? this.delayMin : option.equals("delay_max") ? this.delayMax : null;
	}
	
	//
	
	public void setDelayMin(long delay)
	{
		this.delayMin = delay;
	}
	
	public void setDelayMax(long delay)
	{
		this.delayMax = delay;
	}
}
