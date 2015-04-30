package eu.ha3.mc.presencefootsteps.engine.interfaces;

import java.util.Set;

import eu.ha3.mc.presencefootsteps.game.system.Association;

/* x-placeholder-wtfplv2 */

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
	 * @param location
	 * @param acousticName
	 * @param event
	 */
	public void playAcoustic(Object location, Association acousticName, EventType event);
	
	/**
	 * Plays a acoustic with additional options.
	 * 
	 * @param location
	 * @param acousticName
	 * @param event
	 * @param options
	 */
	public void playAcoustic(Object location, Association acousticName, EventType event, Options options);
	
	public void playAcoustic(Object location, String acousticName, EventType event, Options options);
	
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
	
	/**
	 * Tells if such acoustic exist.
	 * 
	 * @param acoustic
	 * @return
	 */
	public boolean hasAcoustic(String acoustic);
	
	/**
	 * Run various things, such as queued sounds.
	 */
	public void think();
}