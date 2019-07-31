package eu.ha3.presencefootsteps.sound.generator;

import net.minecraft.entity.player.PlayerEntity;

/**
 * Has the ability to generate footsteps based on a Player.
 *
 * @author Hurry
 *
 */
public interface StepSoundGenerator {
    /**
     * Generate footsteps sounds of the Entity.
     */
    void generateFootsteps(PlayerEntity ply);
}
