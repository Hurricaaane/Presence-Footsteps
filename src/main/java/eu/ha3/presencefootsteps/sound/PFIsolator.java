package eu.ha3.presencefootsteps.sound;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import eu.ha3.presencefootsteps.config.Variator;
import eu.ha3.presencefootsteps.sound.player.StepSoundPlayer;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticLibrary;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticsPlayer;
import eu.ha3.presencefootsteps.sound.generator.StepSoundGenerator;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import eu.ha3.presencefootsteps.world.GolemLookup;
import eu.ha3.presencefootsteps.world.Lookup;
import eu.ha3.presencefootsteps.world.PFSolver;
import eu.ha3.presencefootsteps.world.PrimitiveLookup;
import eu.ha3.presencefootsteps.world.Solver;
import eu.ha3.presencefootsteps.world.StateLookup;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;

public class PFIsolator implements Isolator, SoundPlayer {

    private final Map<UUID, StepSoundGenerator> generators = new HashMap<>();

    private final SoundEngine engine;

    private final Variator variator = new Variator();

    private final Lookup<EntityType<?>> golemMap = new GolemLookup();

    private final Lookup<BlockState> blockMap = new StateLookup();

    private final Lookup<BlockSoundGroup> primitiveMap = new PrimitiveLookup();

    private final AcousticsPlayer acoustics = new AcousticsPlayer(this);

    private final Solver solver = new PFSolver(this);

    public PFIsolator(SoundEngine engine) {
        this.engine = engine;
    }

    @Override
    public void playSound(Entity location, String soundName, float volume, float pitch, Options options) {
        acoustics.playSound(location, soundName, volume * engine.getGlobalVolume(), pitch, options);
    }

    @Override
    public Random getRNG() {
        return acoustics.getRNG();
    }

    @Override
    public void think() {
        acoustics.think();
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
    public Lookup<EntityType<?>> getGolemMap() {
        return golemMap;
    }

    @Override
    public Lookup<BlockState> getBlockMap() {
        return blockMap;
    }

    @Override
    public Lookup<BlockSoundGroup> getPrimitiveMap() {
        return primitiveMap;
    }

    @Override
    public StepSoundPlayer getStepPlayer() {
        return acoustics;
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
    public void onFrame(PlayerEntity ply) {
        getGenerator(ply).generateFootsteps(ply);
        think(); // Delayed sounds
    }
}
