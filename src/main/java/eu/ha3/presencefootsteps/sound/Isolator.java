package eu.ha3.presencefootsteps.sound;

import eu.ha3.presencefootsteps.config.Variator;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticLibrary;
import eu.ha3.presencefootsteps.sound.player.StepSoundPlayer;
import eu.ha3.presencefootsteps.world.Lookup;
import eu.ha3.presencefootsteps.world.Solver;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;

public interface Isolator {

    void onFrame(PlayerEntity ply);

    AcousticLibrary getAcoustics();

    Solver getSolver();

    Lookup<BlockState> getBlockMap();

    Lookup<BlockSoundGroup> getPrimitiveMap();

    StepSoundPlayer getStepPlayer();

    Variator getVariator();
}