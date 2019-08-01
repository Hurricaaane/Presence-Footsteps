package eu.ha3.presencefootsteps.util;

import java.util.Random;

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

    public float random(Random rand) {
        return MathUtil.randAB(rand, min, max);
    }

    public float on(float value) {
        return MathUtil.between(min, max, value);
    }
}
