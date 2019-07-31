package eu.ha3.presencefootsteps.config;

import java.lang.reflect.Field;
import java.util.Map;
import eu.ha3.presencefootsteps.PresenceFootsteps;

/**
 * Configurable variables used by the solver.
 */
public class Variator {
    /**
     * The maximum time a player can me immobile before PF picks it up as stopped
     */
    public int IMMOBILE_DURATION = 200;

    /**
     * Shortest interval before playing an immobile sound effect
     */
    public int IMOBILE_INTERVAL_MIN = 500;

    /**
     * Longest interval before playing an immobile sound effect
     */
    public int IMOBILE_INTERVAL_MAX = 3000;

    /**
     * Play sounds based on jumping and landing
     */
    public boolean EVENT_ON_JUMP = true;

    /**
     * The maximum distance a player must fall before it is recognised as a hrad
     * fall
     */
    public float LAND_HARD_DISTANCE_MIN = 0.9f;

    /**
     * Maximum speed for which PF will play sounds for a standing jump
     */
    public float SPEED_TO_JUMP_AS_MULTIFOOT = 0.005F;

    /**
     * The speed a play must be going for PF to recognise it as running
     */
    public float SPEED_TO_RUN = 0.022F;

    /**
     * Normal step distance for humans
     */
    public float DISTANCE_HUMAN = 0.95F;

    /**
     * Step distance when walking on stairs
     */
    public float DISTANCE_STAIR = 0.95F * 0.65F;

    /**
     * Step distance when walking on ladders
     */
    public float DISTANCE_LADDER = 0.5F;

    /**
     * True if PF should play 'wandering' sounds
     */
    public boolean PLAY_WANDER = true;

    /**
     * Maximum distance the player can fall and still play wing folding sounds.
     */
    public float HUGEFALL_LANDING_DISTANCE_MAX = 12;

    /**
     * Minimum distance the player must fall in order to play wing folding sounds.
     */
    public float HUGEFALL_LANDING_DISTANCE_MIN = 3;

    /**
     * Minimum horizontal flight velocity before the player starts dashing.
     */
    public float MIN_DASH_MOTION = 0.8F;

    /**
     * Minimum horizontal flight velocity before the player starts coasting.
     */
    public float MIN_COAST_MOTION = 0.4F;

    /**
     * Minimum horizontal velocity for general flying. Also used when taking off.
     */
    public float MIN_MOTION_HOR = 0.2F;

    /**
     * Minimum vertical flight velocity that a player is considered to be ascending.
     */
    public float MIN_MOTION_Y = 0.2F;

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
     * Wing flap rate whilst coasting. Will alternate between this and the normal
     * rate. (smaller is faster)
     */
    public int WING_SPEED_COAST = 2000;

    /**
     * Number of ticks taken to transition to no sound when the player stops moving.
     */
    public int WING_IMMOBILE_FADE_DURATION = 20000;

    /**
     * Number of ticks before wing sounds start to fade after the player stops
     * moving.
     */
    public int WING_IMMOBILE_FADE_START = 20000;

    /**
     * Number of ticks between when the player takes off and flapping can begin.
     */
    public int WING_JUMPING_REST_TIME = 700;

    public void load(ConfigReader config) {
        Map<String, Property> keys = config.sheet();

        // I am feeling SUPER LAZY today
        Field[] fields = Variator.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                String lowercaseField = field.getName().toLowerCase();

                if (keys.containsKey(lowercaseField)) {
                    if (field.getType() == Float.TYPE) {
                        field.setFloat(this, keys.get(lowercaseField).getFloat());
                    } else if (field.getType() == Integer.TYPE) {
                        field.setInt(this, keys.get(lowercaseField).getInteger());
                    } else if (field.getType() == Boolean.TYPE) {
                        field.setBoolean(this, keys.get(lowercaseField).getBoolean());
                    }
                }
            } catch (Throwable e) {
                PresenceFootsteps.logger.error("Incompatible type: " + e.getClass().getName() + ": " + field.getName(), e);
            }
        }
    }
}
