package eu.ha3.presencefootsteps.sound.acoustics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import net.minecraft.entity.Entity;

class SimultaneousAcoustic implements Acoustic {

    protected final List<Acoustic> acoustics = new ArrayList<>();

    public SimultaneousAcoustic(JsonObject json, AcousticsJsonParser context) {
        JsonArray sim = json.getAsJsonArray("array");
        Iterator<JsonElement> iter = sim.iterator();

        while (iter.hasNext()) {
            JsonElement subElement = iter.next();
            acoustics.add(context.solveAcoustic(subElement));
        }
    }

    @Override
    public void playSound(SoundPlayer player, Entity location, State event, Options inputOptions) {
        acoustics.forEach(acoustic -> acoustic.playSound(player, location, event, inputOptions));
    }
}
