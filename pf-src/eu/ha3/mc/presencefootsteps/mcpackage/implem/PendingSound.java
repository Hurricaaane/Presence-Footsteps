package eu.ha3.mc.presencefootsteps.mcpackage.implem;

import eu.ha3.mc.presencefootsteps.engine.interfaces.Options;
import eu.ha3.mc.presencefootsteps.engine.interfaces.SoundPlayer;

public class PendingSound {
	private Object location;
	private String soundName;
	private float volume;
	private float pitch;
	private Options options;
	private long timeToPlay;
	private long maximum;
	
	public PendingSound(Object location, String soundName, float volume, float pitch, Options options, long timeToPlay, long maximum) {
		this.location = location;
		this.soundName = soundName;
		this.volume = volume;
		this.pitch = pitch;
		this.options = options;
		
		this.timeToPlay = timeToPlay;
		this.maximum = maximum;
	}
	
	/**
	 * Play the sound stored in this pending sound.
	 */
	public void playSound(SoundPlayer player) {
		player.playSound(location, soundName, volume, pitch, options);
	}
	
	/**
	 * Returns the time after which this sound plays.
	 */
	public long getTimeToPlay() {
		return timeToPlay;
	}
	
	/**
	 * Get the maximum delay of this sound, for threshold purposes. If the value is negative, the sound will not be skippable.
	 */
	public long getMaximumBase() {
		return maximum;
	}
}
