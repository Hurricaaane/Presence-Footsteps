package eu.ha3.mc.presencefootsteps.engine.implem;

import java.util.Random;

import eu.ha3.mc.presencefootsteps.engine.interfaces.Acoustic;
import eu.ha3.mc.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.mc.presencefootsteps.engine.interfaces.Options;
import eu.ha3.mc.presencefootsteps.engine.interfaces.SoundPlayer;

public class BasicAcoustic implements Acoustic {
	protected String soundName;
	protected float volMin = 1f;
	protected float volMax = 1f;
	protected float pitchMin = 1f;
	protected float pitchMax = 1f;
	
	protected Options outputOptions;
	
	@Override
	public void playSound(SoundPlayer player, Object location, EventType event, Options inputOptions) {
		if (!soundName.isEmpty()) { // Special case for intentionally empty sounds (as opposed to fall back sounds)
			float volume = generateVolume(player.getRNG());
			float pitch = generatePitch(player.getRNG());
			if (inputOptions != null) {
				if (inputOptions.hasOption("gliding_volume")) {
					volume = volMin + (volMax - volMin) * (Float) inputOptions.getOption("gliding_volume");
				}
				if (inputOptions.hasOption("gliding_pitch")) {
					pitch = pitchMin + (pitchMax - pitchMin) * (Float) inputOptions.getOption("gliding_pitch");
				}
			}
			player.playSound(location, soundName, volume, pitch, outputOptions);
		}
	}
	
	private float generateVolume(Random rng) {
		return randAB(rng, volMin, volMax);
	}
	
	private float generatePitch(Random rng) {
		return randAB(rng, pitchMin, pitchMax);
	}
	
	private float randAB(Random rng, float a, float b) {
		return a >= b ? a : a + rng.nextFloat() * (b - a);
	}
	
	public void setSoundName(String val) {
		soundName = val;
	}
	
	public void setVolMin(float val) {
		volMin = val;
	}
	
	public void setVolMax(float val) {
		volMax = val;
	}
	
	public void setPitchMin(float val) {
		pitchMin = val;
	}
	
	public void setPitchMax(float val) {
		pitchMax = val;
	}
	
}
