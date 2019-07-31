package eu.ha3.presencefootsteps;

import java.io.IOException;
import java.nio.file.Path;

import eu.ha3.presencefootsteps.config.Properties;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import net.minecraft.util.math.MathHelper;

public class PFConfig {

    private final Properties config = new Properties();

    private boolean enabled = true;

    private boolean enableMP = true;

    private Path file;

    public void load(Path file) {
        this.file = file;

        config.setProperty("user.volume", 70);
        config.setProperty("user.enabled", true);
        config.setProperty("user.multiplayer", true);

        config.setProperty("custom.stance", 0);
        config.setProperty("mlp.detected", MineLP.hasPonies());

        load();
    }

    public void load() {
        try {
            config.load(file);
        } catch (IOException e) {
            PresenceFootsteps.logger.warn("Error whilst loading config", e);
        }

        enabled = config.getProperty("user.enabled").getBoolean();
        enableMP = config.getProperty("user.multiplayer").getBoolean();
    }

    public boolean toggleMultiplayer() {
        config.setProperty("user.multiplayer", enableMP = !enableMP);
        save();
        return enableMP;
    }

    public boolean toggleEnabled() {
        config.setProperty("user.enabled", enabled = !enabled);
        save();

        return enabled;
    }

    public Locomotion setLocomotion(Locomotion loco) {
        config.setProperty("custom.stance", loco);
        save();

        return loco;
    }

    public Locomotion getLocomotion() {
        return Locomotion.byName(config.getProperty("custom.stance").getString());
    }

    public boolean getEnabledMP() {
        return enableMP && getEnabled();
    }

    public boolean getEnabled() {
        return enabled;
    }

    public int getVolume() {
        return MathHelper.clamp(config.getProperty("user.volume").getInteger(), 0, 100);
    }

    public void setVolume(int volume) {
        config.setProperty("user.volume", volume);
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            PresenceFootsteps.logger.warn("Error whilst saving config", e);
        }
    }
}
