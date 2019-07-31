package eu.ha3.presencefootsteps.util;

import java.util.Random;

public class MathUtil {
    public static float randAB(Random rng, float a, float b) {
        return a >= b ? a : a + rng.nextFloat() * (b - a);
    }

    public static long randAB(Random rng, long a, long b) {
        return a >= b ? a : a + rng.nextInt((int) b + 1);
    }

    public static float clamp(float min, float max, float value) {
        return min + (max - min) * value;
    }
}
