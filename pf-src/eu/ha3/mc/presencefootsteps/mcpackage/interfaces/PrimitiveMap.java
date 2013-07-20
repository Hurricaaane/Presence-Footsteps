package eu.ha3.mc.presencefootsteps.mcpackage.interfaces;

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

public interface PrimitiveMap
{
	/**
	 * This will return null if the primitive is not defined.
	 * 
	 * @param block
	 * @param meta
	 * @return
	 */
	public abstract String getPrimitiveMap(String primitive);
	
	/**
	 * This will return null if the substrate does not resolve.
	 * 
	 * @param carpet
	 * @param meta
	 * @param event
	 * @return
	 */
	public abstract String getPrimitiveMapSubstrate(String primitive, String substrate);
	
	/**
	 * Register an primitivemap entry.
	 * 
	 * @param key
	 * @param value
	 */
	public abstract void register(String key, String value);
}
