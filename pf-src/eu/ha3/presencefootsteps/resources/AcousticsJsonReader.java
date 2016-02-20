package eu.ha3.presencefootsteps.resources;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.ha3.presencefootsteps.engine.implem.BasicAcoustic;
import eu.ha3.presencefootsteps.engine.implem.DelayedAcoustic;
import eu.ha3.presencefootsteps.engine.implem.EventSelectorAcoustics;
import eu.ha3.presencefootsteps.engine.implem.ProbabilityWeightsAcoustic;
import eu.ha3.presencefootsteps.engine.implem.SimultaneousAcoustic;
import eu.ha3.presencefootsteps.engine.interfaces.Acoustic;
import eu.ha3.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.presencefootsteps.engine.interfaces.Library;

/**
 * A JSON parser that creates a Library of Acoustics.
 * 
 * @author Hurry
 */
public class AcousticsJsonReader {
	private final int ENGINEVERSION = 0;
	
	private String soundRoot;
	
	private float default_volMin;
	private float default_volMax;
	private float default_pitchMin;
	private float default_pitchMax;
	
	private final float DIVIDE = 100f;
	
	public AcousticsJsonReader(String root) {
		soundRoot = root;
	}
	
	public void parseJSON(String jasonString, Library lib) {
		try {
			parseJSONUnsafe(jasonString, lib);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void parseJSONUnsafe(String jsonString, Library lib) throws JsonParseException {
		JsonObject json = new JsonParser().parse(jsonString).getAsJsonObject();
		
		if (!json.get("type").getAsString().equals("library")) throw new JsonParseException("Invalid type: \"library\"");
		if (json.get("engineversion").getAsInt() != ENGINEVERSION) throw new JsonParseException("Unrecognised Engine version: " + ENGINEVERSION + " expected, got " + json.get("engineversion").getAsInt());
		if (!json.has("contents")) throw new JsonParseException("Empty contents");
		
		if (json.has("soundroot")) {
			soundRoot += json.get("soundroot").getAsString();
		}
		
		default_volMin = 1f;
		default_volMax = 1f;
		default_pitchMin = 1f;
		default_pitchMax = 1f;
		
		if (json.has("defaults")) {
			JsonObject defaults = json.getAsJsonObject("defaults");
			if (defaults.has("vol_min")) {
				default_volMin = processPitchOrVolume(defaults, "vol_min");
			}
			if (defaults.has("vol_max")) {
				default_volMax = processPitchOrVolume(defaults, "vol_max");
			}
			if (defaults.has("pitch_min")) {
				default_pitchMin = processPitchOrVolume(defaults, "pitch_min");
			}
			if (defaults.has("pitch_max")) {
				default_pitchMax = processPitchOrVolume(defaults, "pitch_max");
			}
		}
		
		JsonObject contents = json.getAsJsonObject("contents");
		for (Entry<String, JsonElement> preAcoustics : contents.entrySet()) {
			String acousticsName = preAcoustics.getKey();
			JsonObject acousticsDefinition = preAcoustics.getValue().getAsJsonObject();
			EventSelectorAcoustics selector = new EventSelectorAcoustics(acousticsName);
			parseSelector(selector, acousticsDefinition);
			lib.addAcoustic(selector);
		}
	}
	
	private void parseSelector(EventSelectorAcoustics selector, JsonObject acousticsDefinition) throws JsonParseException {
		for (EventType i : EventType.values()) {
			String eventName = i.jsonName();
			if (acousticsDefinition.has(eventName)) {
				JsonElement unsolved = acousticsDefinition.get(eventName);
				Acoustic acoustic = solveAcoustic(unsolved);
				selector.setAcousticPair(i, acoustic);
			}
		}
	}
	
	private Acoustic solveAcoustic(JsonElement unsolved) throws JsonParseException {
		Acoustic ret = null;
		
		if (unsolved.isJsonObject()) {
			ret = solveAcousticsCompound(unsolved.getAsJsonObject());
		} else if (unsolved.isJsonPrimitive() && unsolved.getAsJsonPrimitive().isString()) { // Is a sound name
			BasicAcoustic a = new BasicAcoustic();
			prepareDefaults(a);
			setupSoundName(a, unsolved.getAsString());
			ret = a;
		}
		
		if (ret == null) throw new JsonParseException("Unresolved Json element: \r\n" + unsolved.toString());
		return ret;
	}
	
	private Acoustic solveAcousticsCompound(JsonObject unsolved) throws JsonParseException {
		Acoustic ret = null;
		
		if (!unsolved.has("type") || unsolved.get("type").getAsString().equals("basic")) {
			BasicAcoustic a = new BasicAcoustic();
			prepareDefaults(a);
			setupClassics(a, unsolved);
			ret = a;
		} else {
			String type = unsolved.get("type").getAsString();
			if (type.equals("simultaneous")) {
				List<Acoustic> acoustics = new ArrayList<Acoustic>();
				
				JsonArray sim = unsolved.getAsJsonArray("array");
				Iterator<JsonElement> iter = sim.iterator();
				while (iter.hasNext()) {
					JsonElement subElement = iter.next();
					acoustics.add(solveAcoustic(subElement));
				}
				
				SimultaneousAcoustic a = new SimultaneousAcoustic(acoustics);
				
				ret = a;
			} else if (type.equals("delayed")) {
				DelayedAcoustic a = new DelayedAcoustic();
				prepareDefaults(a);
				setupClassics(a, unsolved);
				
				if (unsolved.has("delay")) {
					a.setDelayMin(unsolved.get("delay").getAsInt());
					a.setDelayMax(unsolved.get("delay").getAsInt());
				} else {
					a.setDelayMin(unsolved.get("delay_min").getAsInt());
					a.setDelayMax(unsolved.get("delay_max").getAsInt());
				}
				
				ret = a;
			} else if (type.equals("probability")) {
				List<Integer> weights = new ArrayList<Integer>();
				List<Acoustic> acoustics = new ArrayList<Acoustic>();
				
				JsonArray sim = unsolved.getAsJsonArray("array");
				Iterator<JsonElement> iter = sim.iterator();
				while (iter.hasNext()) {
					JsonElement subElement = iter.next();
					weights.add(subElement.getAsInt());
					
					if (!iter.hasNext()) throw new JsonParseException("Probability has odd number of children!");
					
					subElement = iter.next();
					acoustics.add(solveAcoustic(subElement));
				}
				
				ProbabilityWeightsAcoustic a = new ProbabilityWeightsAcoustic(acoustics, weights);
				
				ret = a;
			}
		}
		
		return ret;
	}
	
	private void prepareDefaults(BasicAcoustic a) {
		a.setVolMin(this.default_volMin);
		a.setVolMax(this.default_volMax);
		a.setPitchMin(this.default_pitchMin);
		a.setPitchMax(this.default_pitchMax);
	}
	
	private void setupSoundName(BasicAcoustic a, String soundName) {
		if (soundName.charAt(0) != '@') {
			a.setSoundName(this.soundRoot + soundName);
		} else {
			a.setSoundName(soundName.replace("@", ""));
		}
	}
	
	private void setupClassics(BasicAcoustic a, JsonObject solved) {
		setupSoundName(a, solved.get("name").getAsString());
		if (solved.has("vol_min")) {
			a.setVolMin(processPitchOrVolume(solved, "vol_min"));
		}
		if (solved.has("vol_max")) {
			a.setVolMax(processPitchOrVolume(solved, "vol_max"));
		}
		if (solved.has("pitch_min")) {
			a.setPitchMin(processPitchOrVolume(solved, "pitch_min"));
		}
		if (solved.has("pitch_max")) {
			a.setPitchMax(processPitchOrVolume(solved, "pitch_max"));
		}
	}
	
	private float processPitchOrVolume(JsonObject object, String param) {
		return object.get(param).getAsFloat() / DIVIDE;
	}
}
