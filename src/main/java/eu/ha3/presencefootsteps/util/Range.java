package eu.ha3.presencefootsteps.util;

import java.util.Random;

import com.google.gson.JsonObject;

import eu.ha3.presencefootsteps.sound.acoustics.AcousticsJsonParser;

public class Range {

    public float min;
    public float max;

    public Range(float value) {
        this(value, value);
    }

    public Range(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public void copy(Range from) {
        min = from.min;
        max = from.max;
    }

    public void read(String name, JsonObject json, AcousticsJsonParser context) {
        if (json.has(name + "_min")) {
            min = context.getPercentage(json, name + "_min");
        }

        if (json.has(name + "_max")) {
            max = context.getPercentage(json, name + "_max");
        }

        if (json.has(name)) {
            min = max = context.getPercentage(json, name);
        }
    }

    public float random(Random rand) {
        return MathUtil.randAB(rand, min, max);
    }

    public float on(float value) {
        return MathUtil.between(min, max, value);
    }
}
