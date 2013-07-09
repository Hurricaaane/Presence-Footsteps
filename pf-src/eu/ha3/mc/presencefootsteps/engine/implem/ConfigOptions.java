package eu.ha3.mc.presencefootsteps.engine.implem;

import java.util.HashMap;
import java.util.Map;

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
