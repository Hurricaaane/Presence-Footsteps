package eu.ha3.mc.presencefootsteps.interfaces;

import java.util.Set;

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

public interface Library
{
	/**
	 * Adds a acoustic to the library.
	 * 
	 * @param acoustic
	 */
	public void addAcoustic(NamedAcoustic acoustic);
	
	/**
	 * Plays a acoustic.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param event
	 * @param acousticName
	 */
	public void playAcoustic(double x, double y, double z, EventType event, String acousticName);
	
	/**
	 * Gets a key set of the acoustic.
	 * 
	 * @return
	 */
	public Set<String> getAcousticsKeySet();
	
	/**
	 * Gets a specific acoustic. Returns null if it does not exist.
	 * 
	 * @param acoustic
	 * @return
	 */
	public Acoustic getAcoustic(String acoustic);
	
}