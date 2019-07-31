package eu.ha3.presencefootsteps.sound;

import eu.ha3.presencefootsteps.config.Variator;
import eu.ha3.presencefootsteps.sound.player.StepSoundPlayer;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticLibrary;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import eu.ha3.presencefootsteps.world.Lookup;
import eu.ha3.presencefootsteps.world.Solver;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;

public interface Isolator {

    void reset();

    void onFrame(PlayerEntity ply);

    AcousticLibrary getAcoustics();

    void setAcoustics(AcousticLibrary acoustics);

    Solver getSolver();

    void setSolver(Solver solver);

    Lookup<BlockState> getBlockMap();

    Lookup<String> getPrimitiveMap();

    SoundPlayer getSoundPlayer();

    void setSoundPlayer(SoundPlayer soundPlayer);

    StepSoundPlayer getStepPlayer();

    void setStepPlayer(StepSoundPlayer player);

    Variator getVariator();
}