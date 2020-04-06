package eu.ha3.presencefootsteps.sound;

import java.util.Random;
import eu.ha3.presencefootsteps.config.Variator;
import eu.ha3.presencefootsteps.sound.player.StepSoundPlayer;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticLibrary;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticsPlayer;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import eu.ha3.presencefootsteps.world.GolemLookup;
import eu.ha3.presencefootsteps.world.Index;
import eu.ha3.presencefootsteps.world.LocomotionLookup;
import eu.ha3.presencefootsteps.world.Lookup;
import eu.ha3.presencefootsteps.world.PFSolver;
import eu.ha3.presencefootsteps.world.PrimitiveLookup;
import eu.ha3.presencefootsteps.world.Solver;
import eu.ha3.presencefootsteps.world.StateLookup;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.BlockSoundGroup;

public class PFIsolator implements Isolator, SoundPlayer {

    private final SoundEngine engine;

    private final Variator variator = new Variator();

    private final Index<Entity, Locomotion> locomotionMap = new LocomotionLookup();

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
    public Index<Entity, Locomotion> getLocomotionMap() {
        return locomotionMap;
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
}
