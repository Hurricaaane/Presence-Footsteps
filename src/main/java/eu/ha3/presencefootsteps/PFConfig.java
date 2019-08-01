package eu.ha3.presencefootsteps;

import java.nio.file.Path;

import eu.ha3.presencefootsteps.config.JsonFile;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import net.minecraft.util.math.MathHelper;

public class PFConfig extends JsonFile {

    private int volume = 70;

    private String stance = "UNKNOWN";

    private boolean enabled = true;
    private boolean multiplayer = true;

    public PFConfig(Path file) {
        super(file);
    }

    public boolean toggleMultiplayer() {
        multiplayer = !multiplayer;
        save();

        return multiplayer;
    }

    public boolean toggleEnabled() {
        enabled = !enabled;
        save();

        return enabled;
    }

    public Locomotion setLocomotion(Locomotion loco) {
        stance = loco.name();
        save();

        return loco;
    }

    public Locomotion getLocomotion() {
        return Locomotion.byName(stance);
    }

    public boolean getEnabledMP() {
        return multiplayer && getEnabled();
    }

    public boolean getEnabled() {
        return getVolume() > 0 && enabled;
    }

    public int getVolume() {
        return MathHelper.clamp(volume, 0, 100);
    }

    public float setVolume(float volume) {
        this.volume = volume > 97 ? 100 : volume < 3 ? 0 : (int)volume;
        save();

        return getVolume();
    }
}
