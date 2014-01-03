package eu.ha3.mc.presencefootsteps.mcpackage.interfaces;

import net.minecraft.src.EntityPlayer;
import eu.ha3.mc.presencefootsteps.engine.interfaces.EventType;

/* x-placeholder-wtfplv2 */

public interface Solver
{
	/**
	 * Play an association.
	 * 
	 * @param ply
	 * @param assos
	 * @param eventType
	 */
	public abstract void playAssociation(EntityPlayer ply, String assos, EventType eventType);
	
	/**
	 * Find an association for a player particular foot. This will fetch the
	 * player angle and use it as a basis to find out what block is below their
	 * feet (or which block is likely to be below their feet if the player is
	 * walking on the edge of a block when walking over non-emitting blocks like
	 * air or water).<br>
	 * <br>
	 * Returns null if no blocks are valid emitting blocks.<br>
	 * Returns a string that begins with "_NO_ASSOCIATION" if a matching block
	 * was found, but has no association in the blockmap.
	 * 
	 * @param ply
	 * @param verticalOffsetAsMinus
	 * @param isRightFoot
	 */
	public abstract
		String findAssociationForPlayer(EntityPlayer ply, double verticalOffsetAsMinus, boolean isRightFoot);
	
	/**
	 * Find an association for a player. This will take the block right below
	 * the center of the player (or which block is likely to be below them if
	 * the player is walking on the edge of a block when walking over
	 * non-emitting blocks like air or water).<br>
	 * <br>
	 * Returns null if no blocks are valid emitting blocks.<br>
	 * Returns a string that begins with "_NO_ASSOCIATION" if a matching block
	 * was found, but has no association in the blockmap.
	 * 
	 * @param ply
	 * @param verticalOffsetAsMinus
	 * @param isRightFoot
	 */
	public abstract String findAssociationForPlayer(EntityPlayer ply, double verticalOffsetAsMinus);
	
	/**
	 * Find an association for a player, and a location. This will try to find
	 * the best matching block on that location, or near that location, for
	 * instance if the player is walking on the edge of a block when walking
	 * over non-emitting blocks like air or water)<br>
	 * <br>
	 * Returns null if no blocks are valid emitting blocks.<br>
	 * Returns a string that begins with "_NO_ASSOCIATION" if a matching block
	 * was found, but has no association in the blockmap.
	 * 
	 * @param ply
	 * @param xx
	 * @param yy
	 * @param zz
	 * @return
	 */
	public abstract String findAssociationForLocation(EntityPlayer ply, int xx, int yy, int zz);
	
	/**
	 * Find an association for a certain block assuming the player is standing
	 * on it. This will sometimes select the block above because some block act
	 * like carpets. This also applies when the block targeted by the location
	 * is actually not emitting, such as lilypads on water.<br>
	 * <br>
	 * Returns null if the block is not a valid emitting block (this causes the
	 * engine to continue looking for valid blocks). This also happens if the
	 * carpet is non-emitting.<br>
	 * Returns a string that begins with "_NO_ASSOCIATION" if the block is
	 * valid, but has no association in the blockmap. If the carpet was
	 * selected, this solves to the carpet.
	 * 
	 * @param ply
	 * @param xx
	 * @param yy
	 * @param zz
	 * @return
	 */
	public abstract String findAssociationForBlock(int xx, int yy, int zz);
	
	/**
	 * Play special sounds that must stop the usual footstep figuring things out
	 * process.
	 * 
	 * @param ply
	 */
	public abstract boolean playSpecialStoppingConditions(EntityPlayer ply);
	
	/**
	 * Tells if footsteps can be played.
	 * 
	 * @param ply
	 */
	public abstract boolean hasSpecialStoppingConditions(EntityPlayer ply);
	
}