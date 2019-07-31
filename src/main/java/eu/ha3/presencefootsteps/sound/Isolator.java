package eu.ha3.presencefootsteps.sound;

import eu.ha3.presencefootsteps.config.Variator;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticLibrary;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import eu.ha3.presencefootsteps.sound.player.StepSoundPlayer;
import eu.ha3.presencefootsteps.world.Lookup;
import eu.ha3.presencefootsteps.world.Solver;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;

public interface Isolator {

    void onFrame(PlayerEntity ply);

    AcousticLibrary getAcoustics();

    Solver getSolver();

    Lookup<BlockState> getBlockMap();

    Lookup<String> getPrimitiveMap();

    SoundPlayer getSoundPlayer();

    StepSoundPlayer getStepPlayer();

    Variator getVariator();
}