package eu.ha3.presencefootsteps.sound.acoustics;

import java.util.HashMap;
import java.util.Map;

import eu.ha3.presencefootsteps.PresenceFootsteps;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.Isolator;
import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.player.ImmediateSoundPlayer;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import net.minecraft.entity.Entity;

public class AcousticsPlayer extends ImmediateSoundPlayer implements AcousticLibrary {

    private final SoundPlayer player;

    private final Map<String, Acoustic> acoustics = new HashMap<>();

    public AcousticsPlayer(Isolator isolator) {
        player = isolator.getSoundPlayer();
    }

    @Override
    public void addAcoustic(NamedAcoustic acoustic) {
        acoustics.put(acoustic.getName(), acoustic);
    }

    @Override
    public void playAcoustic(Entity location, String acousticName, State event, Options inputOptions) {
        if (acousticName.contains(",")) {
            String[] fragments = acousticName.split(",");

            for (String fragment : fragments) {
                playAcoustic(location, fragment, event, inputOptions);
            }
        } else if (!acoustics.containsKey(acousticName)) {
            PresenceFootsteps.logger.warn("Tried to play a missing acoustic: " + acousticName);
        } else {
            acoustics.get(acousticName).playSound(player, location, event, inputOptions);
        }
    }
}
