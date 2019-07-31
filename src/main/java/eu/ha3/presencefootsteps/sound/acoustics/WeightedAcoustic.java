package eu.ha3.presencefootsteps.sound.acoustics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import eu.ha3.presencefootsteps.config.Options;
import eu.ha3.presencefootsteps.resources.AcousticsJsonReader;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;

public class WeightedAcoustic implements Acoustic {

    protected final List<Acoustic> theAcoustics;

    protected final float[] probabilityThresholds;

    public WeightedAcoustic(List<Acoustic> acoustics, List<Integer> weights) {
        theAcoustics = new ArrayList<>(acoustics);
        probabilityThresholds = new float[acoustics.size() - 1];

        float total = 0;
        for (int i = 0; i < weights.size(); i++) {
            if (weights.get(i) < 0) {
                throw new IllegalArgumentException("A probability weight can't be negative");
            }

            total = total + weights.get(i);
        }

        for (int i = 0; i < weights.size() - 1; i++) {
            probabilityThresholds[i] = weights.get(i) / total;
        }
    }

    @Override
    public void playSound(SoundPlayer player, Object location, State event, Options inputOptions) {
        float rand = player.getRNG().nextFloat();
        int marker = 0;

        while (marker < probabilityThresholds.length && probabilityThresholds[marker] < rand) {
            marker++;
        }
        theAcoustics.get(marker).playSound(player, location, event, inputOptions);
    }

    public static Acoustic fromJson(JsonObject json, AcousticsJsonReader context) {
        List<Integer> weights = new ArrayList<>();
        List<Acoustic> acoustics = new ArrayList<>();

        JsonArray sim = json.getAsJsonArray("array");
        Iterator<JsonElement> iter = sim.iterator();

        while (iter.hasNext()) {
            JsonElement subElement = iter.next();
            weights.add(subElement.getAsInt());

            if (!iter.hasNext()) {
                throw new JsonParseException("Probability has odd number of children!");
            }

            subElement = iter.next();
            acoustics.add(context.solveAcoustic(subElement));
        }

        return new WeightedAcoustic(acoustics, weights);
    }
}
