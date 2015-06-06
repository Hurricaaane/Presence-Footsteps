package eu.ha3.mc.presencefootsteps.engine.implem;

import eu.ha3.mc.presencefootsteps.engine.interfaces.Options;

public class DelayedAcoustic extends BasicAcoustic implements Options {
	protected long delayMin = 0;
	protected long delayMax = 0;
	
	public DelayedAcoustic() {
		super();
		outputOptions = this;
	}
	
	@Override
	public boolean hasOption(String option) {
		return option.equals("delay_min") || option.equals("delay_max");
	}
	
	@SuppressWarnings("null")
	@Override
	public Object getOption(String option) {
		return option.equals("delay_min") ? delayMin : option.equals("delay_max") ? delayMax : null;
	}
	
	public void setDelayMin(long delay) {
		delayMin = delay;
	}
	
	public void setDelayMax(long delay) {
		delayMax = delay;
	}
}
