package eu.ha3.presencefootsteps.sound.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import eu.ha3.presencefootsteps.sound.Isolator;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;

public enum Locomotion {
    UNKNOWN(BipedalStepSoundGenerator::new),
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

    private final Function<Isolator, StepSoundGenerator> constructor;

    private final String translationKey = "menu.pf.stance." + name().toLowerCase();

    Locomotion(Function<Isolator, StepSoundGenerator> gen) {
        constructor = gen;
    }

    public StepSoundGenerator supplyGenerator(Isolator isolator) {
        return constructor.apply(isolator);
    }

    public boolean isBiped() {
        return this == UNKNOWN || this == BIPED;
    }

    public boolean isEquine() {
        return !isBiped();
    }

    public boolean isFlying() {
        return this == FLYING;
    }

    public String getDisplayName() {
        return I18n.translate("menu.pf.stance", I18n.translate(translationKey));
    }

    public static Locomotion byName(String name) {
        return registry.getOrDefault(name, UNKNOWN);
    }

    public static Locomotion forPlayer(PlayerEntity ply, Locomotion preference) {
        if (preference == UNKNOWN) {

            if (!(ply instanceof ClientPlayerEntity && MineLP.hasPonies())) {
                return Locomotion.BIPED;
            }

            return MineLP.getLocomotion(ply);
        }

        return preference;
    }
}
