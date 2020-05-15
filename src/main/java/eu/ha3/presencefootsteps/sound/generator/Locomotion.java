package eu.ha3.presencefootsteps.sound.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public enum Locomotion {
    NONE(() -> StepSoundGenerator.EMPTY),
    BIPED(BipedalStepSoundGenerator::new),
    QUADRUPED(QuadrapedalStepSoundGenerator::new),
    FLYING(PegasusStepSoundGenerator::new);

    private static final Map<String, Locomotion> registry = new HashMap<>();

    static {
        for (Locomotion i : values()) {
            registry.put(i.name(), i);
            registry.put(String.valueOf(i.ordinal()), i);
        }
    }

    private final Supplier<StepSoundGenerator> constructor;

    private final String translationKey = "menu.pf.stance." + name().toLowerCase();

    Locomotion(Supplier<StepSoundGenerator> gen) {
        constructor = gen;
    }

    public StepSoundGenerator supplyGenerator() {
        return constructor.get();
    }

    public String getDisplayName() {
        return I18n.translate("menu.pf.stance", I18n.translate(translationKey));
    }

    public static Locomotion byName(String name) {
        return registry.getOrDefault(name, BIPED);
    }

    public static Locomotion forLiving(Entity entity, Locomotion fallback) {
        if (MineLP.hasPonies()) {
            return MineLP.getLocomotion(entity, fallback);
        }

        return fallback;
    }

    public static Locomotion forPlayer(PlayerEntity ply, Locomotion preference) {
        if (preference == NONE) {
            if (ply instanceof ClientPlayerEntity && MineLP.hasPonies()) {
                return MineLP.getLocomotion(ply);
            }

            return Locomotion.BIPED;
        }

        return preference;
    }
}
