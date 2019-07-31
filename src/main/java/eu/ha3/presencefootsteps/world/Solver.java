package eu.ha3.presencefootsteps.world;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import eu.ha3.presencefootsteps.sound.State;

/**
 * Solves in-world locations and players into associations. Associations are an
 * extension of Acoustic names, with some special codes. They are derived from
 * the blockmap and defined with these values:<br>
 * <br>
 * The association null is derived from blockmap "NOT_EMITTER" and means the
 * block is NOT MEANT to emit sounds (not equal to "no sound").<br>
 * The association "_NO_ASSOCIATION:xx:yy:zz" is derived from AN ABSENCE of an
 * entry in the blockmap (after solving missing metadata and carpets). xx,yy,zz
 * is the location of the incremented block.<br>
 * Any other association string returned by the findAssociation* methods
 * correspond to an Acoustic name.
 *
 * @author Hurry
 */
public interface Solver {
    /**
     * Play an association.
     */
    void playAssociation(PlayerEntity ply, @Nullable Association assos, State eventType);

    /**
     * Find an association for a player particular foot. This will fetch the player
     * angle and use it as a basis to find out what block is below their feet (or
     * which block is likely to be below their feet if the player is walking on the
     * edge of a block when walking over non-emitting blocks like air or water).<br>
     * <br>
     * Returns null if no blocks are valid emitting blocks.<br>
     * Returns a string that begins with "_NO_ASSOCIATION" if a matching block was
     * found, but has no association in the blockmap.
     */
    @Nullable
    Association findAssociation(PlayerEntity ply, double verticalOffsetAsMinus, boolean isRightFoot);

    /**
     * Find an association for a player. This will take the block right below the
     * center of the player (or which block is likely to be below them if the player
     * is walking on the edge of a block when walking over non-emitting blocks like
     * air or water).<br>
     * <br>
     * Returns null if no blocks are valid emitting blocks.<br>
     * Returns a string that begins with "_NO_ASSOCIATION" if a matching block was
     * found, but has no association in the blockmap.
     */
    @Nullable
    Association findAssociation(PlayerEntity ply, double verticalOffsetAsMinus);

    /**
     * Find an association for a player, and a location. This will try to find the
     * best matching block on that location, or near that location, for instance if
     * the player is walking on the edge of a block when walking over non-emitting
     * blocks like air or water)<br>
     * <br>
     * Returns null if no blocks are valid emitting blocks.<br>
     * Returns a string that begins with "_NO_ASSOCIATION" if a matching block was
     * found, but has no association in the blockmap.
     */
    @Nullable
    Association findAssociation(PlayerEntity ply, int xx, int yy, int zz);

    /**
     * Find an association for a certain block assuming the player is standing on
     * it. This will sometimes select the block above because some block act like
     * carpets. This also applies when the block targeted by the location is
     * actually not emitting, such as lilypads on water.<br>
     * <br>
     * Returns null if the block is not a valid emitting block (this causes the
     * engine to continue looking for valid blocks). This also happens if the carpet
     * is non-emitting.<br>
     * Returns a string that begins with "_NO_ASSOCIATION" if the block is valid,
     * but has no association in the blockmap. If the carpet was selected, this
     * solves to the carpet.
     */
    @Nullable
    Association findAssociation(World w, int xx, int yy, int zz);

    /**
     * Find an association for a certain block assuming the player is standing on
     * it, using a custom strategy which strategies are defined by the solver.
     */
    @Nullable
    Association findAssociation(World w, int xx, int yy, int zz, String strategy);

    /**
     * Play special sounds that must stop the usual footstep figuring things out
     * process.
     */
    boolean playStoppingConditions(PlayerEntity ply);

    /**
     * Tells if footsteps can be played.
     */
    boolean hasStoppingConditions(PlayerEntity ply);

}