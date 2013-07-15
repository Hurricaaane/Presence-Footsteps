package eu.ha3.mc.presencefootsteps.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.ha3.mc.presencefootsteps.engine.implem.BasicAcoustic;
import eu.ha3.mc.presencefootsteps.engine.implem.DelayedAcoustic;
import eu.ha3.mc.presencefootsteps.engine.implem.EventSelectorAcoustics;
import eu.ha3.mc.presencefootsteps.engine.implem.ProbabilityWeightsAcoustic;
import eu.ha3.mc.presencefootsteps.engine.implem.SimultaneousAcoustic;
import eu.ha3.mc.presencefootsteps.engine.interfaces.Acoustic;
import eu.ha3.mc.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.mc.presencefootsteps.engine.interfaces.Library;

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

/**
 * JASON? JAAAASOOON?<br>
 * <a href="http://youtu.be/i7IE9gLwLUU?t=1m28s">http://youtu.
 * be/i7IE9gLwLUU?t=1m28s</a><br>
 * <br>
 * A JSON parser that creates a Library of Acoustics.
 * 
 * @author Hurry
 * 
 */
public class JasonAcoustics_Engine0
{
	private final int ENGINEVERSION = 0;
	
	private String soundRoot;
	
	private final Map<String, EventType> equivalents;
	
	private float default_volMin;
	private float default_volMax;
	private float default_pitchMin;
	private float default_pitchMax;
	
	private final float DIVIDE = 100f;
	
	public JasonAcoustics_Engine0(String soundRoot)
	{
		this.soundRoot = soundRoot;
		
		this.equivalents = new HashMap<String, EventType>();
		this.equivalents.put("wander", EventType.WANDER);
		this.equivalents.put("walk", EventType.WALK);
		this.equivalents.put("run", EventType.RUN);
		this.equivalents.put("jump", EventType.JUMP);
		this.equivalents.put("land", EventType.LAND);
		this.equivalents.put("swim", EventType.SWIM);
		this.equivalents.put("climb", EventType.CLIMB);
		this.equivalents.put("up", EventType.UP);
		this.equivalents.put("down", EventType.DOWN);
		this.equivalents.put("climb_run", EventType.CLIMB_RUN);
		this.equivalents.put("up_run", EventType.UP_RUN);
		this.equivalents.put("down_run", EventType.DOWN_RUN);
	}
	
	public void parseJSON(String jasonString, Library lib)
	{
		try
		{
			parseJSONUnsafe(jasonString, lib);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void parseJSONUnsafe(String jasonString, Library lib) throws UnexpectedDataException
	{
		JsonObject jason = new JsonParser().parse(jasonString).getAsJsonObject();
		
		if (!jason.get("type").getAsString().equals("library"))
			throw new UnexpectedDataException();
		
		if (jason.get("engineversion").getAsInt() != this.ENGINEVERSION)
			throw new UnexpectedDataException();
		
		if (!jason.has("contents"))
			throw new UnexpectedDataException();
		
		if (jason.has("soundroot"))
		{
			this.soundRoot = this.soundRoot + jason.get("soundroot").getAsString();
		}
		
		this.default_volMin = 1f;
		this.default_volMax = 1f;
		this.default_pitchMin = 1f;
		this.default_pitchMax = 1f;
		
		if (jason.has("defaults"))
		{
			JsonObject defaults = jason.getAsJsonObject("defaults");
			this.default_volMin =
				defaults.has("vol_min") ? processPitchOrVolume(defaults, "vol_min") : this.default_volMin;
			this.default_volMax =
				defaults.has("vol_max") ? processPitchOrVolume(defaults, "vol_max") : this.default_volMax;
			this.default_pitchMin =
				defaults.has("pitch_min") ? processPitchOrVolume(defaults, "pitch_min") : this.default_pitchMin;
			this.default_pitchMax =
				defaults.has("pitch_max") ? processPitchOrVolume(defaults, "pitch_max") : this.default_pitchMax;
		}
		
		JsonObject contents = jason.getAsJsonObject("contents");
		for (Entry<String, JsonElement> preAcoustics : contents.entrySet())
		{
			String acousticsName = preAcoustics.getKey();
			JsonObject acousticsDefinition = preAcoustics.getValue().getAsJsonObject();
			
			EventSelectorAcoustics selector = new EventSelectorAcoustics(acousticsName);
			parseSelector(selector, acousticsDefinition);
			
			lib.addAcoustic(selector);
		}
	}
	
	private void parseSelector(EventSelectorAcoustics selector, JsonObject acousticsDefinition)
		throws UnexpectedDataException
	{
		for (String eventName : this.equivalents.keySet())
		{
			if (acousticsDefinition.has(eventName))
			{
				JsonElement unsolved = acousticsDefinition.get(eventName);
				
				Acoustic acoustic = solveAcoustic(unsolved);
				
				selector.setAcousticPair(this.equivalents.get(eventName), acoustic);
			}
		}
	}
	
	private Acoustic solveAcoustic(JsonElement unsolved) throws UnexpectedDataException
	{
		Acoustic ret = null;
		
		if (unsolved.isJsonObject())
		{
			ret = solveAcousticsCompound(unsolved.getAsJsonObject());
		}
		else if (unsolved.isJsonPrimitive() && unsolved.getAsJsonPrimitive().isString())
		{
			// Is a sound name
			BasicAcoustic a = new BasicAcoustic();
			prepareDefaults(a);
			
			setupSoundName(a, unsolved.getAsString());
			
			ret = a;
		}
		
		if (ret == null)
			throw new UnexpectedDataException();
		
		return ret;
	}
	
	private Acoustic solveAcousticsCompound(JsonObject unsolved) throws UnexpectedDataException
	{
		Acoustic ret = null;
		
		if (!unsolved.has("type") || unsolved.get("type").getAsString().equals("basic"))
		{
			BasicAcoustic a = new BasicAcoustic();
			prepareDefaults(a);
			setupClassics(a, unsolved);
			
			ret = a;
		}
		else
		{
			String type = unsolved.get("type").getAsString();
			
			if (type.equals("simultaneous"))
			{
				List<Acoustic> acoustics = new ArrayList<Acoustic>();
				
				JsonArray sim = unsolved.getAsJsonArray("array");
				for (Iterator<JsonElement> iter = sim.iterator(); iter.hasNext();)
				{
					JsonElement subElement = iter.next();
					acoustics.add(solveAcoustic(subElement));
				}
				
				SimultaneousAcoustic a = new SimultaneousAcoustic(acoustics);
				
				ret = a;
			}
			else if (type.equals("delayed"))
			{
				DelayedAcoustic a = new DelayedAcoustic();
				prepareDefaults(a);
				setupClassics(a, unsolved);
				
				if (unsolved.has("delay"))
				{
					a.setDelayMin(unsolved.get("delay").getAsInt());
					a.setDelayMax(unsolved.get("delay").getAsInt());
				}
				else
				{
					a.setDelayMin(unsolved.get("delay_min").getAsInt());
					a.setDelayMax(unsolved.get("delay_max").getAsInt());
				}
				
				ret = a;
			}
			else if (type.equals("probability"))
			{
				List<Integer> weights = new ArrayList<Integer>();
				List<Acoustic> acoustics = new ArrayList<Acoustic>();
				
				JsonArray sim = unsolved.getAsJsonArray("array");
				Iterator<JsonElement> iter = sim.iterator();
				while (iter.hasNext())
				{
					JsonElement subElement = iter.next();
					weights.add(subElement.getAsInt());
					
					if (!iter.hasNext())
						throw new UnexpectedDataException();
					
					subElement = iter.next();
					acoustics.add(solveAcoustic(subElement));
				}
				
				ProbabilityWeightsAcoustic a = new ProbabilityWeightsAcoustic(acoustics, weights);
				
				ret = a;
			}
		}
		
		return ret;
	}
	
	private void prepareDefaults(BasicAcoustic a)
	{
		a.setVolMin(this.default_volMin);
		a.setVolMax(this.default_volMax);
		a.setPitchMin(this.default_pitchMin);
		a.setPitchMax(this.default_pitchMax);
	}
	
	private void setupSoundName(BasicAcoustic a, String soundName)
	{
		if (!soundName.startsWith("@"))
		{
			a.setSoundName(this.soundRoot + soundName);
		}
		else
		{
			a.setSoundName(soundName.replace("@", ""));
		}
	}
	
	private void setupClassics(BasicAcoustic a, JsonObject solved)
	{
		setupSoundName(a, solved.get("name").getAsString());
		
		if (solved.has("vol_min"))
		{
			a.setVolMin(processPitchOrVolume(solved, "vol_min"));
		}
		if (solved.has("vol_max"))
		{
			a.setVolMax(processPitchOrVolume(solved, "vol_max"));
		}
		if (solved.has("pitch_min"))
		{
			a.setPitchMin(processPitchOrVolume(solved, "pitch_min"));
		}
		if (solved.has("pitch_max"))
		{
			a.setPitchMax(processPitchOrVolume(solved, "pitch_max"));
		}
	}
	
	private float processPitchOrVolume(JsonObject object, String param)
	{
		return object.get(param).getAsFloat() / this.DIVIDE;
	}
	
	//
	//
	//
	//
	//
	//
	//
	//
}
