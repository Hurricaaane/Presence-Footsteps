package eu.ha3.mc.presencefootsteps.mod;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.src.PFHaddon;
import eu.ha3.util.property.simple.ConfigProperty;

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

public class Variator
{
	public boolean FORCE_HUMANOID = false;
	public float HUMAN_DISTANCE = 0.95f;
	
	public int WING_JUMPING_REST_TIME = 700;
	public int WING_SLOW = 550;
	public int WING_FAST = 550 - 350;
	public float WING_SPEED_MIN = 0.2f;
	public float WING_SPEED_MAX = 0.2f + 0.25f;
	public int WING_IMMOBILE_FADE_START = 20000;
	public int WING_IMMOBILE_FADE_DURATION = 20000;
	public float WING_VOLUME = 0.5f;
	public float WING_PITCH_RADIUS = 0.05f;
	
	public float LANDING_PITCH_RADIUS = 0.2f;
	public float DASHING_PITCH_RADIUS = 0.1f;
	
	public float GROUND_AIR_STATE_SPEED = 0.2f;
	public float GROUND_AIR_STATE_CHANGE_VOLUME = 0.3f;
	public float HUGEFALL_LANDING_VOLUME_MIN = 0.1f;
	public float HUGEFALL_LANDING_VOLUME_MAX = 0.5f;
	public float HUGEFALL_LANDING_DISTANCE_MIN = 3f;
	public float HUGEFALL_LANDING_DISTANCE_MAX = 3f + 9f;
	
	public float WALK_DISTANCE = 0.65f;
	public float WALK_CHASING_FACTOR = 1f / 7f;
	public float SLOW_DISTANCE = 0.75f;
	public float GALLOP_DISTANCE_1 = 0.80f;
	public float GALLOP_DISTANCE_2 = 0.25f;
	public float GALLOP_DISTANCE_3 = 0.25f;
	public float GALLOP_DISTANCE_4 = 0.05f;
	public boolean GALLOP_3STEP = true;
	public float LADDER_DISTANCE = 0.4f;
	public float STAIRCASE_DISTANCE = 0.01f;
	public float STAIRCASE_ANTICHASE_DIFFERENCE = 1f;
	
	public float GLOBAL_VOLUME_MULTIPLICATOR = 1f;
	public float MATSTEPS_VOLUME_MULTIPLICATOR = 0.25f;
	public float MATSTEP_PITCH_RADIUS = 1f;
	public float WALK_VOLUME = 1f;
	public float SLOW_VOLUME = 1f;
	public float GALLOP_VOLUME = 1f;
	public float STAIRCASE_VOLUME = 1f;
	public float LADDER_VOLUME = 1f;
	
	public float SPEED_TO_WALK = 0.08f;
	public float SPEED_TO_GALLOP = 0.13f;
	
	public boolean PLAY_OVERRIDES = false;
	public boolean PLAY_MATSTEPS = true;
	public boolean PLAY_BLOCKSTEPS = true;
	
	public float JUMP_VOLUME = 1f;
	public boolean PLAY_STEP_ON_JUMP = true;
	public float LAND_HARD_VOLUME = 1f;
	public float LAND_HARD_DISTANCE_MIN = 0.9f; //2f
	public boolean PLAY_STEP_ON_LAND_HARD = true;
	
	public boolean PLAY_SPECIAL_ON_JUMP = false;
	public boolean PLAY_SPECIAL_ON_LAND = false; // TODO UNUSED
	public boolean PLAY_SPECIAL_ON_LAND_HARD = false;
	public int IMMOBILE_DURATION = 700;
	
	public void loadConfig(ConfigProperty config)
	{
		Set<String> keysFromConfig = config.getAllProperties().keySet();
		Set<String> keys = new HashSet<String>();
		for (String key : keysFromConfig)
		{
			keys.add(key.toUpperCase());
		}
		
		// I am feeling SUPER LAZY today
		Field[] fields = Variator.class.getDeclaredFields();
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
				PFHaddon.log("Incompatible type: " + e.getClass().getName() + ": " + field.getName());
			}
		}
	}
}
