package eu.ha3.mc.presencefootsteps.mcpackage.interfaces;

/* x-placeholder-wtfplv2 */

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
