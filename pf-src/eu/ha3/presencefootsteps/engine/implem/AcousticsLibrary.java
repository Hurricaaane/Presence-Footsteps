package eu.ha3.presencefootsteps.engine.implem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import eu.ha3.presencefootsteps.engine.interfaces.Acoustic;
import eu.ha3.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.presencefootsteps.engine.interfaces.Library;
import eu.ha3.presencefootsteps.engine.interfaces.NamedAcoustic;
import eu.ha3.presencefootsteps.engine.interfaces.Options;
import eu.ha3.presencefootsteps.engine.interfaces.SoundPlayer;
import eu.ha3.presencefootsteps.game.Association;
import eu.ha3.presencefootsteps.log.PFLog;

public abstract class AcousticsLibrary implements Library {
	private Map<String, Acoustic> acoustics;
	
	public AcousticsLibrary() {
		acoustics = new HashMap<String, Acoustic>();
	}
	
	@Override
	public void addAcoustic(NamedAcoustic acoustic) {
		acoustics.put(acoustic.getName(), acoustic);
	}
	
	@Override
	public Set<String> getAcousticsKeySet() {
		return acoustics.keySet();
	}
	
	@Override
	public Acoustic getAcoustic(String acoustic) {
		if (acoustics.containsKey(acoustic)) {
			return acoustics.get(acoustic);
		}
		return null;
	}
	
	@Override
	public void playAcoustic(Object location, Association acousticName, EventType event) {
		playAcoustic(location, acousticName, event, null);
	}
	
	@Override
	public void playAcoustic(Object location, Association acousticName, EventType event, Options inputOptions) {
		if (acousticName.getData().contains(",")) {
			String fragments[] = acousticName.getData().split(",");
			for (String fragment : fragments) {
				playAcoustic(location, fragment, event, inputOptions);
			}
		} else if (!acoustics.containsKey(acousticName.getData())) {
			onAcousticNotFound(location, acousticName.getData(), event, inputOptions);
		} else {
			PFLog.debug("  Playing acoustic " + acousticName.getData() + " for event " + event.toString().toUpperCase());
			acoustics.get(acousticName.getData()).playSound(mySoundPlayer(), location, event, inputOptions);
		}
	}
	
	public void playAcoustic(Object location, String acousticName, EventType event, Options inputOptions) {
		if (acousticName.contains(",")) {
			String fragments[] = acousticName.split(",");
			for (String fragment : fragments) {
				playAcoustic(location, fragment, event, inputOptions);
			}
		} else if (!acoustics.containsKey(acousticName)) {
			onAcousticNotFound(location, acousticName, event, inputOptions);
		} else {
			PFLog.debug("  Playing acoustic " + acousticName + " for event " + event.toString().toUpperCase());
			acoustics.get(acousticName).playSound(mySoundPlayer(), location, event, inputOptions);
		}
	}
	
	@Override
	public boolean hasAcoustic(String acousticName) {
		return acoustics.containsKey(acousticName);
	}
	
	protected abstract void onAcousticNotFound(Object location, String acousticName, EventType event, Options inputOptions);
	
	protected abstract SoundPlayer mySoundPlayer();
}
