package eu.ha3.presencefootsteps.engine.implem;

import eu.ha3.presencefootsteps.engine.interfaces.Options;

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
	
	@Override
	public Object getOption(String option) {
		return option.equals("delay_min") ? delayMin : option.equals("delay_max") ? delayMax : null;
	}
	
	@Override
	public DelayedAcoustic withOption(String option, Object value) {
		if (option.equals("delay_min")) setDelayMin((long)value);
		if (option.equals("delay_max")) setDelayMax((long)value);
		return this;
	}
	
	public void setDelayMin(long delay) {
		delayMin = delay;
	}
	
	public void setDelayMax(long delay) {
		delayMax = delay;
	}
}
