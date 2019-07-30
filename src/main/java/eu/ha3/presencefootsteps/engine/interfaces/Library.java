package eu.ha3.presencefootsteps.engine.interfaces;

import java.util.Set;

import eu.ha3.presencefootsteps.game.Association;

public interface Library {
	/**
	 * Adds an acoustic to the library.
	 */
	public void addAcoustic(NamedAcoustic acoustic);
	
	/**
	 * Plays an acoustic.
	 */
	public void playAcoustic(Object location, Association acousticName, EventType event);
	
	/**
	 * Plays an acoustic with additional options.
	 */
	public void playAcoustic(Object location, Association acousticName, EventType event, Options options);
	
	public void playAcoustic(Object location, String acousticName, EventType event, Options options);
	
	/**
	 * Gets a key set of the acoustic.
	 */
	public Set<String> getAcousticsKeySet();
	
	/**
	 * Gets a specific acoustic. Returns null if it does not exist.
	 */
	public Acoustic getAcoustic(String acoustic);
	
	/**
	 * Checks if such an acoustic exist.
	 */
	public boolean hasAcoustic(String acoustic);
	
	/**
	 * Run various things, such as queued sounds.
	 */
	public void think();
}