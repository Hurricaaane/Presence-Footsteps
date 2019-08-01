package eu.ha3.presencefootsteps.world;

import java.util.Locale;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import eu.ha3.presencefootsteps.sound.Isolator;
import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.State;

public class PFSolver implements Solver {

    private static final Logger logger = LogManager.getLogger("PFSolver");

    private static final double TRAP_DOOR_OFFSET = 0.1;

    private final Isolator isolator;

    public PFSolver(Isolator isolator) {
        this.isolator = isolator;
    }

    @Override
    public void playAssociation(PlayerEntity ply, Association assos, State eventType) {
        if (assos.isNotEmitter()) {
            return;
        }

        assos = assos.at(ply);

        if (assos.hasAssociation()) {
            isolator.getAcoustics().playAcoustic(assos, eventType);
        } else {
            isolator.getStepPlayer().playStep(assos);
        }
    }

    @Override
    public Association findAssociation(PlayerEntity ply, double verticalOffsetAsMinus, boolean isRightFoot) {

        double rot = Math.toRadians(MathHelper.wrapDegrees(ply.yaw));

        Vec3d pos = ply.getPos();

        float feetDistanceToCenter = 0.2f * (isRightFoot ? -1 : 1);

        return findAssociation(ply, new BlockPos(
            pos.x + Math.cos(rot) * feetDistanceToCenter,
            ply.getBoundingBox().minY - TRAP_DOOR_OFFSET - verticalOffsetAsMinus,
            pos.z + Math.sin(rot) * feetDistanceToCenter
        ));
    }

    private Association findAssociation(PlayerEntity player, BlockPos pos) {

        if (player.isInWater()) {
            logger.warn("Playing a sound while in the water! This is supposed to be halted by the stopping conditions!!");
        }

        if (!(player instanceof OtherClientPlayerEntity)) {
            Vec3d vel = player.getVelocity();

            if ((vel.x != 0 || vel.y != 0 || vel.z != 0) && Math.abs(vel.y) < 0.02) {
                return Association.NOT_EMITTER; // Don't play sounds on every tiny bounce
            }
        }

        Association worked = findAssociation(player.world, pos);

        // If it didn't work, the player has walked over the air on the border of a block.
        // ------ ------ --> z
        // | o | < player is here
        // wool | air |
        // ------ ------
        // |
        // V z
        if (!worked.isNull()) {
            return worked;
        }

        // Create a trigo. mark contained inside the block the player is over
        double xdang = (player.x - pos.getX()) * 2 - 1;
        double zdang = (player.z - pos.getZ()) * 2 - 1;
        // -1 0 1
        // ------- -1
        // | o |
        // | + | 0 --> x
        // | |
        // ------- 1
        // |
        // V z

        // If the player is at the edge of that
        if (Math.max(Math.abs(xdang), Math.abs(zdang)) <= 0.2f) {
            return worked;
        }
        // Find the maximum absolute value of X or Z
        boolean isXdangMax = Math.abs(xdang) > Math.abs(zdang);
        // --------------------- ^ maxofZ-
        // | . . |
        // | . . |
        // | o . . |
        // | . . |
        // | . |
        // < maxofX- maxofX+ >
        // Take the maximum border to produce the sound
        if (isXdangMax) { // If we are in the positive border, add 1, else subtract 1
            worked = findAssociation(player.world, pos.east(xdang > 0 ? 1 : -1));
        } else {
            worked = findAssociation(player.world, pos.north(zdang > 0 ? 1 : -1));
        }

        // If that didn't work, then maybe the footstep hit in the
        // direction of walking
        // Try with the other closest block
        if (!worked.isNull()) {
            return worked;
        }

        // Take the maximum direction and try with the orthogonal direction of it
        if (isXdangMax) {
            return findAssociation(player.world, pos.north(zdang > 0 ? 1 : -1));
        }

        return findAssociation(player.world, pos.east(xdang > 0 ? 1 : -1));
    }

    private Association findAssociation(World world, BlockPos pos) {
        BlockState in = world.getBlockState(pos);

        BlockState above = world.getBlockState(pos.up());
        // Try to see if the block above is a carpet...
        String association = isolator.getBlockMap().getAssociation(above, "carpet");

        if (association == null) {
            association = "NOT_EMITTER";
        }

        if ("NOT_EMITTER".equals(association)) {
            // This condition implies that if the carpet is NOT_EMITTER, solving will
            // CONTINUE with the actual block surface the player is walking on
            Material mat = in.getMaterial();

            if (mat == Material.AIR || mat == Material.PART) {
                BlockState below = world.getBlockState(pos.down());

                association = isolator.getBlockMap().getAssociation(below, "bigger");

                if (association != null) {
                    pos = pos.down();
                    in = below;
                    logger.debug("Fence detected: " + association);
                }
            }

            if (association == null) {
                association = isolator.getBlockMap().getAssociation(in);
            }

            if (!"NOT_EMITTER".equals(association) && association != null) {
                // This condition implies that foliage over a NOT_EMITTER block CANNOT PLAY
                // This block most not be executed if the association is a carpet
                String foliage = isolator.getBlockMap().getAssociation(above, "foliage");

                if (foliage == null) {
                    foliage = "NOT_EMITTER";
                }

                if (!"NOT_EMITTER".equals(foliage)) {
                    association += "," + foliage;
                    logger.debug("Foliage detected: " + foliage);
                }
            }
        } else {
            pos = pos.up();
            in = above;
            logger.debug("Carpet detected: " + association);
        }

        if ("NOT_EMITTER".equals(association)) {
            if (in.getBlock() != Blocks.AIR) {
                logger.debug("Not emitter for %0 : %1", in);
            }

            // Player has stepped on a non-emitter block as defined in the blockmap
            return Association.NOT_EMITTER;
        }

        if (association != null) {
            logger.debug("Found association for %0 : %1 : %2", in, association);
            return new Association(in, pos).associated().with(association);
        }

        String primitive = resolvePrimitive(in);

        if ("NOT_EMITTER".equals(primitive)) {
            logger.debug("Primitive for %0 : %1 : %2 is NOT_EMITTER! Following behavior is uncertain.", in, primitive);
            return Association.NOT_EMITTER;
        }

        Association assos = new Association(in, pos);

        if (primitive != null) {
            logger.debug("Found primitive for %0 : %1 : %2", in, primitive);

            assos.with(primitive);
        }

        logger.debug("No association for %0 : %1", in);

        return assos;
    }

    @Nullable
    private String resolvePrimitive(BlockState state) {
        Block block = state.getBlock();

        if (block.isAir(state)) {
            return "NOT_EMITTER";
        }

        BlockSoundGroup sounds = block.getSoundGroup(state);

        SoundEvent stepSound = sounds.getStepSound();

        String soundName = stepSound.getId().getPath();

        if (soundName.isEmpty()) {
            soundName = "UNDEFINED";
        }

        String substrate = String.format(Locale.ENGLISH, "%.2f_%.2f", sounds.volume, sounds.pitch);

        // Check for primitive in register
        String primitive = isolator.getPrimitiveMap().getAssociation(soundName, substrate);

        if (primitive == null) {
            if (stepSound != null) {
                // Check for break sound
                primitive = isolator.getPrimitiveMap().getAssociation(soundName, "break_" + soundName);
            }
            if (primitive == null) {
                primitive = isolator.getPrimitiveMap().getAssociation(soundName);
            }
        }

        if (primitive != null) {
            logger.debug("Primitive found for " + soundName + ":" + substrate);
            return primitive;
        }

        logger.debug("No primitive for " + soundName + ":" + substrate);
        return null;
    }

    @Override
    public boolean playStoppingConditions(PlayerEntity ply) {
        if (ply.isInWater()) {
            float volume = (float) ply.getVelocity().length() * 0.35F;

            isolator.getAcoustics().playAcoustic(ply, "_SWIM", ply.isInWater() ? State.SWIM : State.WALK, Options.create()
                    .withOption("gliding_volume", volume > 1 ? 1 : volume));
            return true;
        }

        return false;
    }

    @Override
    public boolean hasStoppingConditions(PlayerEntity ply) {
        return ply.isInWater();
    }

    @Override
    public Association findAssociation(World world, BlockPos pos, String strategy) {
        if (!"find_messy_foliage".equals(strategy)) {
            return Association.NOT_EMITTER;
        }

        BlockState above = world.getBlockState(pos.up());

        String foliage = isolator.getBlockMap().getAssociation(above, "foliage");

        if (foliage == null || "NOT_EMITTER".equals(foliage)) {
            return Association.NOT_EMITTER;
        }

        // we discard the normal block association, and mark the foliage as detected
        if ("MESSY_GROUND".equals(isolator.getBlockMap().getAssociation(above, "messy"))) {
            return new Association().associated().with(foliage);
        }

        return Association.NOT_EMITTER;
    }
}
