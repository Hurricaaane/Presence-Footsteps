package eu.ha3.presencefootsteps.sound.player;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import eu.ha3.presencefootsteps.sound.player.StepSoundPlayer;
import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.player.DelayedSoundPlayer;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import eu.ha3.presencefootsteps.world.Association;

/**
 * A Library that can also play sounds and default footsteps.
 *
 * @author Hurry
 */
public class ImmediateSoundPlayer implements SoundPlayer, StepSoundPlayer {

    private final Random random = new Random();

    private final DelayedSoundPlayer delayedPlayer = new DelayedSoundPlayer(this);

    @Override
    public Random getRNG() {
        return random;
    }

    @Override
    public void playStep(LivingEntity entity, Association assos) {
        BlockSoundGroup soundType = assos.getSoundGroup();

        if (!assos.getMaterial().isLiquid() && soundType != null) {
            BlockState beside = entity.world.getBlockState(assos.pos(0, 1, 0));

            if (beside.getBlock() == Blocks.SNOW) {
                soundType = Blocks.SNOW.getSoundGroup(beside);
            }

            playAttenuatedSound(entity, soundType.getStepSound().getId().toString(), soundType.getVolume() * 0.15F, soundType.getPitch());
        }
    }

    @Override
    public void playSound(Object location, String soundName, float volume, float pitch, @Nullable Options options) {
        if (!(location instanceof Entity)) {
            return;
        }

        if (options != null && options.containsKey("delay_min") && options.containsKey("delay_max")) {
            delayedPlayer.playSound(location, soundName, volume, pitch, options);

            return;
        }

        playAttenuatedSound((Entity) location, soundName, volume, pitch);
    }

    private void playAttenuatedSound(Entity location, String soundName, float volume, float pitch) {
        Identifier res = getSoundId(soundName, location);

        PositionedSoundInstance sound = createSound(res, volume, pitch, location);

        MinecraftClient mc = MinecraftClient.getInstance();
        double distance = mc.getCameraEntity().squaredDistanceTo(location);

        if (distance > 100) {
            mc.getSoundManager().play(sound, (int) Math.floor(Math.sqrt(distance) / 2));
        } else {
            mc.getSoundManager().play(sound);
        }
    }

    @Override
    public void think() {
        delayedPlayer.think();
    }

    private boolean isClientPlayer(Entity ply) {
        PlayerEntity clientPlayer = MinecraftClient.getInstance().player;
        return ply.getUuid().equals(clientPlayer.getUuid());
    }

    private PositionedSoundInstance createSound(Identifier id, float volume, float pitch, Entity entity) {
        return new PositionedSoundInstance(id, SoundCategory.MASTER, volume, pitch, false, 0,
                SoundInstance.AttenuationType.LINEAR, (float) entity.x, (float) entity.y, (float) entity.z, false);
    }

    private Identifier getSoundId(String name, Entity location) {
        if (name.indexOf(':') < 0) {
            String domain = "presencefootsteps";

            if (!isClientPlayer(location)) {
                domain += "mono"; // Switch to mono if playing another player
            }

            return new Identifier(domain, name);
        }

        return new Identifier(name);
    }
}
