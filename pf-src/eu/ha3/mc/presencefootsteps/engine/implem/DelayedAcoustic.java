package eu.ha3.mc.presencefootsteps.engine.implem;

import eu.ha3.mc.presencefootsteps.engine.interfaces.Options;

/* x-placeholder-wtfplv2 */

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
