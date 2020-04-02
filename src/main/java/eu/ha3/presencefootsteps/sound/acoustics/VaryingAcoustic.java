package eu.ha3.presencefootsteps.sound.acoustics;

import com.google.gson.JsonObject;

import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import eu.ha3.presencefootsteps.util.Range;
import net.minecraft.entity.Entity;


/**
 * The simplest form of an acoustic. Plays one sound with a set volume and pitch range.
 *
 * @author Hurry
 */
class VaryingAcoustic implements Acoustic {

    private final String soundName;

    private final Range volume = new Range(1);
    private final Range pitch = new Range(1);

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
        soundName = context.getSoundName(name);
    }

    protected Options getOptions() {
        return Options.EMPTY;
    }

    @Override
    public void playSound(SoundPlayer player, Entity location, State event, Options inputOptions) {
        if (soundName.isEmpty()) {
            // Special case for intentionally empty sounds (as opposed to fall back sounds)
            return;
        }

        float volume = this.volume.random(player.getRNG());
        float pitch = this.pitch.random(player.getRNG());

        if (inputOptions.containsKey("gliding_volume")) {
            volume = this.volume.on(inputOptions.get("gliding_volume"));
        }
        if (inputOptions.containsKey("gliding_pitch")) {
            pitch = this.pitch.on(inputOptions.get("gliding_pitch"));
        }

        player.playSound(location, soundName, volume, pitch, getOptions());
    }
}
