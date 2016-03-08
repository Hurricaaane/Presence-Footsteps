package eu.ha3.presencefootsteps.engine.interfaces;

public interface Options {
	public boolean hasOption(String option);
	
	public Object getOption(String option);
	
	public Options withOption(String option, Object value);
}
