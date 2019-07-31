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
import eu.ha3.presencefootsteps.config.ConfigOptions;
import eu.ha3.presencefootsteps.sound.Isolator;
import eu.ha3.presencefootsteps.sound.State;

public class PFSolver implements Solver {

    private static final Logger logger = LogManager.getLogger("PFSolver");

    private static final double TRAP_DOOR_OFFSET = 0.1;

    private final Isolator isolator;

    public PFSolver(Isolator isolator) {
        this.isolator = isolator;
    }

    @Override
    public void playAssociation(PlayerEntity ply, @Nullable Association assos, State eventType) {
        if (assos != null && !assos.isNotEmitter()) {
            if (assos.hasAssociation()) {
                isolator.getAcoustics().playAcoustic(ply, assos, eventType);
            } else {
                isolator.getStepPlayer().playStep(ply, assos);
            }
        }
    }

    @Nullable
    @Override
    public Association findAssociation(PlayerEntity ply, double verticalOffsetAsMinus, boolean isRightFoot) {

        int yy = MathHelper.floor(ply.getBoundingBox().minY - TRAP_DOOR_OFFSET - verticalOffsetAsMinus);

        double rot = Math.toRadians(MathHelper.wrapDegrees(ply.yaw));
        double xn = Math.cos(rot);
        double zn = Math.sin(rot);

        Vec3d pos = ply.getPos();

        float feetDistanceToCenter = 0.2f * (isRightFoot ? -1 : 1);

        int xx = MathHelper.floor(pos.x + xn * feetDistanceToCenter);
        int zz = MathHelper.floor(pos.z + zn * feetDistanceToCenter);

        return findAssociation(ply, xx, yy, zz);
    }

    @Nullable
    @Override
    public Association findAssociation(PlayerEntity ply, double verticalOffsetAsMinus) {

        BlockPos pos = ply.getBlockPos();

        int yy = MathHelper.floor(pos.getY() - ply.getHeightOffset() - TRAP_DOOR_OFFSET - verticalOffsetAsMinus);

        return findAssociation(ply, pos.getX(), yy, pos.getZ());
    }

    @Nullable
    @Override
    public Association findAssociation(PlayerEntity player, int x, int y, int z) {
        if (!(player instanceof OtherClientPlayerEntity)) {
            Vec3d vel = player.getVelocity();
            if ((vel.x != 0 || vel.y != 0 || vel.z != 0)) {
                if (Math.abs(vel.y) < 0.02) {
                    return null; // Don't play sounds on every tiny bounce
                }
            }
        }

        if (player.isInWater()) {
            logger.debug(
                    "WARNING!!! Playing a sound while in the water! This is supposed to be halted by the stopping conditions!!");
        }

        Association worked = findAssociation(player.world, x, y, z);

        // If it didn't work, the player has walked over the air on the border of a
        // block.
        // ------ ------ --> z
        // | o | < player is here
        // wool | air |
        // ------ ------
        // |
        // V z
        if (worked == null) {
            // Create a trigo. mark contained inside the block the player is over
            double xdang = (player.x - x) * 2 - 1;
            double zdang = (player.z - z) * 2 - 1;
            // -1 0 1
            // ------- -1
            // | o |
            // | + | 0 --> x
            // | |
            // ------- 1
            // |
            // V z

            // If the player is at the edge of that
            if (Math.max(Math.abs(xdang), Math.abs(zdang)) > 0.2f) {
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
                    worked = findAssociation(player.world, xdang > 0 ? x + 1 : x - 1, y, z);
                } else {
                    worked = findAssociation(player.world, x, y, zdang > 0 ? z + 1 : z - 1);
                }

                // If that didn't work, then maybe the footstep hit in the
                // direction of walking
                // Try with the other closest block
                if (worked == null) { // Take the maximum direction and try with the orthogonal direction of it
                    if (isXdangMax) {
                        worked = findAssociation(player.world, x, y, zdang > 0 ? z + 1 : z - 1);
                    } else {
                        worked = findAssociation(player.world, xdang > 0 ? x + 1 : x - 1, y, z);
                    }
                }
            }
        }
        return worked;
    }

    @Nullable
    @Override
    public Association findAssociation(World world, int xx, int yy, int zz) {
        BlockState in = world.getBlockState(new BlockPos(xx, yy, zz));

        BlockState above = world.getBlockState(new BlockPos(xx, yy + 1, zz));
        String association = isolator.getBlockMap().getAssociation(above, "carpet"); // Try to see if the block above is
                                                                                     // a carpet...

        if (association == null || association.equals("NOT_EMITTER")) {
            // This condition implies that if the carpet is NOT_EMITTER, solving will
            // CONTINUE with the actual block surface the player is walking on
            // > NOT_EMITTER carpets will not cause solving to skip
            Material mat = in.getMaterial();
            if (mat == Material.AIR || mat == Material.PART) {
                BlockState below = world.getBlockState(new BlockPos(xx, yy - 1, zz));

                association = isolator.getBlockMap().getAssociation(below, "bigger");

                if (association != null) {
                    yy--;
                    in = below;
                    logger.debug("Fence detected: " + association);
                }
            }

            if (association == null) {
                association = isolator.getBlockMap().getAssociation(in);
            }

            if (association != null && !association.equals("NOT_EMITTER")) {
                // This condition implies that foliage over a NOT_EMITTER block CANNOT PLAY
                // This block most not be executed if the association is a carpet
                // => this block of code is here, not outside this if else group.

                String foliage = isolator.getBlockMap().getAssociation(above, "foliage");
                if (foliage != null && !foliage.equals("NOT_EMITTER")) {
                    association += "," + foliage;
                    logger.debug("Foliage detected: " + foliage);
                }
            }
        } else {
            yy++;
            in = above;
            logger.debug("Carpet detected: " + association);
        }

        if (association != null) {
            if (association.contentEquals("NOT_EMITTER")) {
                if (in.getBlock() != Blocks.AIR) { // air block
                    logger.debug("Not emitter for %0 : %1", in);
                }

                return null; // Player has stepped on a non-emitter block as defined in the blockmap
            }

            logger.debug("Found association for %0 : %1 : %2", in, association);
            return new Association(in, xx, yy, zz).setAssociation(association);
        }

        String primitive = resolvePrimitive(in);

        if (primitive != null) {
            if (primitive.contentEquals("NOT_EMITTER")) {
                logger.debug("Primitive for %0 : %1 : %2 is NOT_EMITTER! Following behavior is uncertain.", in,
                        primitive);
                return null;
            }
            logger.debug("Found primitive for %0 : %1 : %2", in, primitive);
            return (new Association(in, xx, yy, zz)).setPrimitive(primitive);
        }
        logger.debug("No association for %0 : %1", in);
        return (new Association(in, xx, yy, zz)).setNoAssociation();
    }

    private String resolvePrimitive(BlockState state) {
        Block block = state.getBlock();

        if (block.isAir(state)) {
            return "NOT_EMITTER"; // air block
        }

        BlockSoundGroup sounds = block.getSoundGroup(state);

        SoundEvent stepSound = sounds.getStepSound();

        String soundName = stepSound.getId().getPath();

        if (soundName.isEmpty()) {
            soundName = "UNDEFINED";
        }

        String substrate = String.format(Locale.ENGLISH, "%.2f_%.2f", sounds.volume, sounds.pitch);

        String primitive = isolator.getPrimitiveMap().getAssociation(soundName, substrate); // Check for primitive in
                                                                                            // register

        if (primitive == null) {
            if (stepSound != null) {
                primitive = isolator.getPrimitiveMap().getAssociation(soundName, "break_" + soundName); // Check for
                                                                                                        // break sound
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
            ConfigOptions options = new ConfigOptions();

            float volume = (float) ply.getVelocity().length() * 0.35F;

            options.getMap().put("gliding_volume", volume > 1 ? 1 : volume);

            isolator.getAcoustics().playAcoustic(ply, "_SWIM", ply.isInWater() ? State.SWIM : State.WALK, options);
            return true;
        }

        return false;
    }

    @Override
    public boolean hasStoppingConditions(PlayerEntity ply) {
        return ply.isInWater();
    }

    @Nullable
    @Override
    public Association findAssociation(World world, int xx, int yy, int zz, String strategy) {
        if (!strategy.equals("find_messy_foliage")) {
            return null;
        }

        BlockState above = world.getBlockState(new BlockPos(xx, yy + 1, zz));

        String association = null;
        boolean found = false;

        String foliage = isolator.getBlockMap().getAssociation(above, "foliage");
        if (foliage != null && !foliage.equals("NOT_EMITTER")) {
            // we discard the normal block association, and mark the foliage as detected
            association = foliage;
            String isMessy = isolator.getBlockMap().getAssociation(above, "messy");

            if (isMessy != null && isMessy.equals("MESSY_GROUND")) {
                found = true;
            }
        }

        if (found && association != null) {
            return association.contentEquals("NOT_EMITTER") ? null : new Association(association);
        }
        return null;
    }
}
