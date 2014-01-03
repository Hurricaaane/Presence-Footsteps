package eu.ha3.mc.presencefootsteps.engine.implem;

import java.util.HashMap;
import java.util.Map;

import eu.ha3.mc.presencefootsteps.engine.interfaces.Options;

/* x-placeholder-wtfplv2 */

public class ConfigOptions implements Options
{
	private Map<String, Object> map;
	
	public ConfigOptions()
	{
		this.map = new HashMap<String, Object>();
	}
	
	public Map<String, Object> getMap()
	{
		return this.map;
	}
	
	@Override
	public boolean hasOption(String option)
	{
		return this.map.containsKey(option);
	}
	
	@Override
	public Object getOption(String option)
	{
		return this.map.get(option);
	}
}
