package eu.ha3.mc.presencefootsteps.mcpackage.interfaces;

public interface PrimitiveMap {
	/**
	 * This will return null if the primitive is not defined.
	 * 
	 * @param primitive
	 * @return
	 */
	public String getPrimitiveMap(String primitive);
	
	/**
	 * This will return null if the substrate does not resolve.
	 * 
	 * @param primitive
	 * @param substrate
	 * @return
	 */
	public String getPrimitiveMapSubstrate(String primitive, String substrate);
	
	/**
	 * Register an primitivemap entry.
	 * 
	 * @param key
	 * @param value
	 */
	public void register(String key, String value);
}
