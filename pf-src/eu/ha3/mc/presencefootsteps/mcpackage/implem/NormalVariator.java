package eu.ha3.mc.presencefootsteps.mcpackage.implem;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import eu.ha3.mc.presencefootsteps.log.PFLog;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Variator;
import eu.ha3.util.property.contract.PropertyHolder;

/* x-placeholder-wtfplv2 */

public class NormalVariator implements Variator
{
	public int IMMOBILE_DURATION = 200;
	
	public boolean EVENT_ON_JUMP = true;
	public float LAND_HARD_DISTANCE_MIN = 0.9f;
	
	public float SPEED_TO_JUMP_AS_MULTIFOOT = 0.005f;
	public float SPEED_TO_RUN = 0.022f;
	
	public float DISTANCE_HUMAN = 0.95f;
	public float DISTANCE_STAIR = 0.95f * 0.65f;
	public float DISTANCE_LADDER = 0.5f;
	
	public boolean PLAY_WANDER = true;
	
	//public boolean FORCE_HUMANOID = false;
	//public boolean GALLOP_3STEP = true;
	//public float GALLOP_DISTANCE_1 = 0.80f;
	//public float GALLOP_DISTANCE_2 = 0.25f;
	//public float GALLOP_DISTANCE_3 = 0.25f;
	//public float GALLOP_DISTANCE_4 = 0.05f;
	//public float GALLOP_VOLUME = 1f;
	
	//public float GROUND_AIR_STATE_SPEED = 0.2f;
	//public float HUGEFALL_LANDING_DISTANCE_MAX = 3f + 9f;
	
	//public float HUGEFALL_LANDING_DISTANCE_MIN = 3f;
	
	//public float SLOW_DISTANCE = 0.75f;
	//public float SLOW_VOLUME = 1f;
	//public float SPEED_TO_GALLOP = 0.13f;
	//public float SPEED_TO_WALK = 0.08f;
	
	//public float STAIRCASE_ANTICHASE_DIFFERENCE = 1f;
	
	//public float WALK_CHASING_FACTOR = 1f / 7f;
	
	//public float WALK_DISTANCE = 0.65f;
	//public int WING_FAST = 550 - 350;
	//public int WING_IMMOBILE_FADE_DURATION = 20000;
	
	//public int WING_IMMOBILE_FADE_START = 20000;
	
	//public int WING_JUMPING_REST_TIME = 700;
	
	//public int WING_SLOW = 550;
	//public float WING_SPEED_MAX = 0.2f + 0.25f;
	//public float WING_SPEED_MIN = 0.2f;
	
	@Override
	public void loadConfig(PropertyHolder config)
	{
		Set<String> keysFromConfig = config.getAllProperties().keySet();
		Set<String> keys = new HashSet<String>();
		for (String key : keysFromConfig)
		{
			keys.add(key.toUpperCase());
		}
		
		// I am feeling SUPER LAZY today
		Field[] fields = NormalVariator.class.getDeclaredFields();
		for (Field field : fields)
		{
			try
			{
				String fieldName = field.getName();
				if (keys.contains(fieldName))
				{
					String lowercaseField = fieldName.toLowerCase();
					if (field.getType() == Float.TYPE)
					{
						field.setFloat(this, config.getFloat(lowercaseField));
					}
					else if (field.getType() == Integer.TYPE)
					{
						field.setInt(this, config.getInteger(lowercaseField));
					}
					else if (field.getType() == Boolean.TYPE)
					{
						field.setBoolean(this, config.getBoolean(lowercaseField));
					}
				}
			}
			catch (Throwable e)
			{
				PFLog.log("Incompatible type: " + e.getClass().getName() + ": " + field.getName());
			}
		}
	}
}
