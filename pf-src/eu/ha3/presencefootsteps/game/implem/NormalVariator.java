package eu.ha3.presencefootsteps.game.implem;

import java.lang.reflect.Field;
import java.util.Set;

import eu.ha3.presencefootsteps.game.interfaces.Variator;
import eu.ha3.presencefootsteps.log.PFLog;
import eu.ha3.util.property.contract.PropertyHolder;

public class NormalVariator implements Variator {
	/**
	 * The maximum time a player can me immobile before PF picks it up as stopped
	 */
	public int IMMOBILE_DURATION = 200;
	
	/**
	 * Play sounds based on jumping and landing
	 */
	public boolean EVENT_ON_JUMP = true;
	
	/**
	 * The maximum distance a player must fall before it is recognised as a hrad fall
	 */
	public float LAND_HARD_DISTANCE_MIN = 0.9f;
	
	/**
	 * Maximum speed for which PF will play sounds for a standing jump
	 */
	public float SPEED_TO_JUMP_AS_MULTIFOOT = 0.005f;
	
	/**
	 * The speed a play must be going for PF to recognise it as running
	 */
	public float SPEED_TO_RUN = 0.022f;
	
	/**
	 * Normal step distance for humans
	 */
	public float DISTANCE_HUMAN = 0.95f;
	
	/**
	 * Step distance when walking on stairs
	 */
	public float DISTANCE_STAIR = 0.95f * 0.65f;
	
	/**
	 * Step distance when walking on ladders
	 */
	public float DISTANCE_LADDER = 0.5f;
	
	/**
	 * True if PF should play 'wandering' sounds
	 */
	public boolean PLAY_WANDER = true;
	
	//public boolean FORCE_HUMANOID = false;
	//public boolean GALLOP_3STEP = true;
	//public float GALLOP_DISTANCE_1 = 0.80f;
	//public float GALLOP_DISTANCE_2 = 0.25f;
	//public float GALLOP_DISTANCE_3 = 0.25f;
	//public float GALLOP_DISTANCE_4 = 0.05f;
	//public float GALLOP_VOLUME = 1f;
	
	/**
	 * Maximum distance the player can fall and still play wing folding sounds.
	 */
	public float HUGEFALL_LANDING_DISTANCE_MAX = 12f;
	/**
	 * Minimum distance the player must fall in order to play wing folding sounds.
	 */
	public float HUGEFALL_LANDING_DISTANCE_MIN = 3f;
	
	//public float SLOW_DISTANCE = 0.75f;
	//public float SLOW_VOLUME = 1f;
	//public float SPEED_TO_GALLOP = 0.13f;
	//public float SPEED_TO_WALK = 0.08f;
	
	//public float STAIRCASE_ANTICHASE_DIFFERENCE = 1f;
	
	//public float WALK_CHASING_FACTOR = 1f / 7f;
	
	/**
	 * Minimum horizontal flight velocity before the player starts dashing.
	 */
	public float MIN_DASH_MOTION = 0.8f;
	
	/**
	 * Minimum horizontal flight velocity before the player starts coasting.
	 */
	public float MIN_COAST_MOTION = 0.4f;
	
	/**
	 * Minimum horizontal velocity for general flying. Also used when taking off.
	 */
	public float MIN_MOTION_HOR = 0.2f;
	
	/**
	 * Minimum vertical flight velocity that a player is considered to be ascending.
	 */
	public float MIN_MOTION_Y = 0.2f;
	
	/**
	 * Ticks buffer used when the player's flight state changes.
	 */
	public int FLIGHT_TRANSITION_TIME = 200;
	
	/**
	 * Wing flap rate when dashing. (smaller is faster)
	 */
	public int WING_SPEED_RAPID = 300;
	
	/**
	 * Wing flap rate during normal flight. (smaller is faster)
	 */
	public int WING_SPEED_NORMAL = 500;
	
	/**
	 * Wing flap rate when hovering. (smaller is faster)
	 */
	public int WING_SPEED_IDLE = 900;
	
	/**
	 * Wing flap rate whilst coasting. Will alternate between this and the normal rate. (smaller is faster)
	 */
	public int WING_SPEED_COAST = 2000;
	
	/**
	 * Number of ticks taken to transition to no sound when the player stops moving.
	 */
	public int WING_IMMOBILE_FADE_DURATION = 20000;
	
	/**
	 * Number of ticks before wing sounds start to fade after the player stops moving.
	 */
	public int WING_IMMOBILE_FADE_START = 20000;
	
	/**
	 * Number of ticks between when the player takes off and flapping can begin.
	 */
	public int WING_JUMPING_REST_TIME = 700;
	
	@Override
	public void loadConfig(PropertyHolder config) {
		Set<String> keys = config.getAllProperties().keySet();
		
		// I am feeling SUPER LAZY today
		Field[] fields = NormalVariator.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				String fieldName = field.getName();
				if (keys.contains(fieldName)) {
					String lowercaseField = fieldName.toLowerCase();
					if (field.getType() == Float.TYPE) {
						field.setFloat(this, config.getFloat(lowercaseField));
					} else if (field.getType() == Integer.TYPE) {
						field.setInt(this, config.getInteger(lowercaseField));
					} else if (field.getType() == Boolean.TYPE) {
						field.setBoolean(this, config.getBoolean(lowercaseField));
					}
				}
			} catch (Throwable e) {
				PFLog.log("Incompatible type: " + e.getClass().getName() + ": " + field.getName());
			}
		}
	}
}
