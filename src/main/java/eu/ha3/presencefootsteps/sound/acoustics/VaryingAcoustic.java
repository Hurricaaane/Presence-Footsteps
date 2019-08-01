package eu.ha3.presencefootsteps.sound.acoustics;

import com.google.gson.JsonObject;

import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import eu.ha3.presencefootsteps.util.Range;
import net.minecraft.entity.Entity;

class VaryingAcoustic implements Acoustic {

    protected String soundName;

    protected final Range volume = new Range(1);
    protected final Range pitch = new Range(1);

    protected Options outputOptions;

    public VaryingAcoustic(JsonObject json, AcousticsJsonParser context) {
        this(json.get("name").getAsString(), context);

        if (json.has("vol_min")) {
            volume.min = context.getPercentage(json, "vol_min");
        }

        if (json.has("vol_max")) {
            volume.max = context.getPercentage(json, "vol_max");
        }

        if (json.has("pitch_min")) {
            pitch.min = context.getPercentage(json, "pitch_min");
        }

        if (json.has("pitch_max")) {
            pitch.max = context.getPercentage(json, "pitch_max");
        }
    }

    public VaryingAcoustic(String name, AcousticsJsonParser context) {
        volume.copy(context.getVolumeRange());
        pitch.copy(context.getPitchRange());
        setSoundName(context.getSoundName(name));
    }

    public void setSoundName(String val) {
        soundName = val;
    }

    public Range getVolumeRange() {
        return volume;
    }

    public Range getPitchRange() {
        return pitch;
    }

    @Override
    public void playSound(SoundPlayer player, Entity location, State event, Options inputOptions) {
        if (!soundName.isEmpty()) { // Special case for intentionally empty sounds (as opposed to fall back sounds)
            float volume = this.volume.random(player.getRNG());
            float pitch = this.pitch.random(player.getRNG());

            if (inputOptions != null) {
                if (inputOptions.containsKey("gliding_volume")) {
                    volume = this.volume.on(inputOptions.get("gliding_volume"));
                }
                if (inputOptions.containsKey("gliding_pitch")) {
                    pitch = this.pitch.on(inputOptions.get("gliding_pitch"));
                }
            }

            player.playSound(location, soundName, volume, pitch, outputOptions);
        }
    }
}
