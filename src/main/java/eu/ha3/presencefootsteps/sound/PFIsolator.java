package eu.ha3.presencefootsteps.sound;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import eu.ha3.presencefootsteps.config.Variator;
import eu.ha3.presencefootsteps.resources.SoundEngine;
import eu.ha3.presencefootsteps.sound.player.StepSoundPlayer;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticLibrary;
import eu.ha3.presencefootsteps.sound.generator.StepSoundGenerator;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import eu.ha3.presencefootsteps.world.LegacyBlockLookup;
import eu.ha3.presencefootsteps.world.Lookup;
import eu.ha3.presencefootsteps.world.PrimitiveLookup;
import eu.ha3.presencefootsteps.world.Solver;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;

public class PFIsolator implements Isolator {

    private final Map<UUID, StepSoundGenerator> generators = new HashMap<>();

    private final SoundEngine engine;

    private final Variator variator = new Variator();

    private final Lookup<BlockState> blockMap = new LegacyBlockLookup();

    private final Lookup<String> primitiveMap = new PrimitiveLookup();

    private AcousticLibrary acoustics;

    private Solver solver;

    private SoundPlayer soundPlayer;

    private StepSoundPlayer defaultStepPlayer;

    public PFIsolator(SoundEngine engine) {
        this.engine = engine;
    }

    @Override
    public AcousticLibrary getAcoustics() {
        return acoustics;
    }

    @Override
    public Solver getSolver() {
        return solver;
    }

    @Override
    public Lookup<BlockState> getBlockMap() {
        return blockMap;
    }

    @Override
    public Lookup<String> getPrimitiveMap() {
        return primitiveMap;
    }

    @Override
    public SoundPlayer getSoundPlayer() {
        return soundPlayer;
    }

    @Override
    public StepSoundPlayer getStepPlayer() {
        return defaultStepPlayer;
    }

    @Override
    public void setAcoustics(AcousticLibrary acoustics) {
        this.acoustics = acoustics;
    }

    @Override
    public void setSolver(Solver solver) {
        this.solver = solver;
    }

    @Override
    public void setSoundPlayer(SoundPlayer soundPlayer) {
        this.soundPlayer = soundPlayer;
    }

    @Override
    public void setStepPlayer(StepSoundPlayer defaultStepPlayer) {
        this.defaultStepPlayer = defaultStepPlayer;
    }

    @Override
    public Variator getVariator() {
        return variator;
    }

    private StepSoundGenerator getGenerator(PlayerEntity ply) {
        if (generators.size() > 10) {
            generators.entrySet().removeIf(entry -> ply.world.getPlayerByUuid(entry.getKey()) == null);
        }

        return generators.computeIfAbsent(ply.getUuid(), uuid -> {
            return engine.supplyGenerator(ply);
        });
    }

    @Override
    public void reset() {
        generators.clear();
    }

    @Override
    public void onFrame(PlayerEntity ply) {
        if (ply.removed) {
            return;
        }

        getGenerator(ply).generateFootsteps(ply);
        acoustics.think(); // Delayed sounds
    }

}
