package eu.ha3.mc.presencefootsteps.game.system;

import java.util.Locale;
import java.util.regex.Pattern;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import eu.ha3.mc.presencefootsteps.engine.implem.ConfigOptions;
import eu.ha3.mc.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.mc.presencefootsteps.log.PFLog;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Isolator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Solver;

/* x-placeholder-wtfplv2 */

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
public class PFSolver implements Solver {
	private final Isolator isolator;
	public static String NO_ASSOCIATION = "_NO_ASSOCIATION";
	public static String NO_ASSOCIATION_SEPARATOR = "*";
	
	// 2014-01-03 : 1.7.2
	// Replaced with star because blocks are unlikely to contain this character
	// 2014-05-22 : 1.7.2
	// Pattern.quote fixes a bug where "*" would be taken as a regex.
	// Instead of escaping \\*, we directly provide a literal pattern
	private final Pattern noAssosPattern = Pattern.compile(Pattern.quote(NO_ASSOCIATION_SEPARATOR));
	
	public PFSolver(Isolator isolator) {
		this.isolator = isolator;
	}
	
	@Override
	public void playAssociation(EntityPlayer ply, Association assos, EventType eventType) {
		if (assos != null) {
			if (assos.getNoAssociation()) {
				isolator.getDefaultStepPlayer().playStep(ply, assos.x, assos.y, assos.z, assos.getBlock());
			} else {
				isolator.getAcoustics().playAcoustic(ply, assos, eventType);
			}
		}
	}
	
	private int toInt(String s) {
		return Integer.parseInt(s);
	}
	
	@Override
	public Association findAssociationForPlayer(EntityPlayer ply, double verticalOffsetAsMinus, boolean isRightFoot) {
		// int yy = MathHelper.floor_double(ply.posY - 0.1d - ply.yOffset - verticalOffsetAsMinus);
		int yy = MathHelper.floor_double(ply.getEntityBoundingBox().minY - 0.1d - verticalOffsetAsMinus); // 0.1d: Support for trapdoors
		
		double rot = Math.toRadians(MathHelper.wrapAngleTo180_float(ply.rotationYaw));
		double xn = Math.cos(rot);
		double zn = Math.sin(rot);
		
		float feetDistanceToCenter = 0.2f * (isRightFoot ? -1 : 1);
		
		int xx = MathHelper.floor_double(ply.posX + xn * feetDistanceToCenter);
		int zz = MathHelper.floor_double(ply.posZ + zn * feetDistanceToCenter);
		
		return findAssociationForLocation(ply, xx, yy, zz);
	}

	@Override
	public Association findAssociationForPlayer(EntityPlayer ply, double verticalOffsetAsMinus) {
		int yy = MathHelper.floor_double(ply.posY - 0.1d - ply.getYOffset() - verticalOffsetAsMinus);
		// 0.1d: Support for trapdoors

		int xx = MathHelper.floor_double(ply.posX);
		int zz = MathHelper.floor_double(ply.posZ);
		return findAssociationForLocation(ply, xx, yy, zz);
	}

	@Override
	public Association findAssociationForLocation(EntityPlayer player, int x, int y, int z) {
		if (player.isInWater()) PFLog.debug("WARNING!!! Playing a sound while in the water! " + "This is supposed to be halted by the stopping conditions!!");

		Association worked = findAssociationForBlock(x, y, z);

		// If it didn't work, the player has walked over the air on the border
		// of a block.
		// ------ ------ --> z
		// | o | < player is here
		// wool | air |
		// ------ ------
		// |
		// V z
		if (worked == null) {
			// Create a trigo. mark contained inside the block the player is
			// over
			double xdang = (player.posX - x) * 2 - 1;
			double zdang = (player.posZ - z) * 2 - 1;
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
				if (isXdangMax) {
					// If we are in the positive border, add 1, else subtract 1
					worked = findAssociationForBlock(xdang > 0 ? x + 1 : x - 1,
							y, z);
				} else {
					worked = findAssociationForBlock(x, y, zdang > 0 ? z + 1
							: z - 1);
				}

				// If that didn't work, then maybe the footstep hit in the
				// direction of walking
				// Try with the other closest block
				if (worked == null) { // Take the maximum direction and try with
										// the orthogonal direction of it
					if (isXdangMax) {
						worked = findAssociationForBlock(x, y,
								zdang > 0 ? z + 1 : z - 1);
					} else {
						worked = findAssociationForBlock(xdang > 0 ? x + 1
								: x - 1, y, z);
					}
				}
			}
		}
		return worked;
	}

	@Override
	public Association findAssociationForBlock(int xx, int yy, int zz) {
		World world = Minecraft.getMinecraft().theWorld;

		// getBlock(x,y,z)
		IBlockState state = world.getBlockState(new BlockPos(xx, yy, zz));
		// air block
		if (state.getBlock() == Blocks.air) {
			// This block of code allows detection of fences

			// int mm = world.blockGetRenderType(xx, yy - 1, zz);
			// see Entity, line 885
			IBlockState below = world.getBlockState(new BlockPos(xx, yy - 1, zz));
			int mm = below.getBlock().getRenderType();
			if (mm == 11 || mm == 32 || mm == 21) {
				state = below;
			}
		}

		// REMOVED: This blocks carpet detection over air
		// If the block is air, it is not an emitter
		// if (block == 0) return null;

		IBlockState above = world.getBlockState(new BlockPos(xx, yy + 1, zz));

		// Try to see if the block above is a carpet...
		String association = isolator.getBlockMap().getBlockMapSubstrate(above, "carpet");

		PFLog.debug("Walking on block: " + PF172Helper.nameOf(state.getBlock()) + " -- Being in block: " + PF172Helper.nameOf(above.getBlock()));

		if (association == null || association.equals("NOT_EMITTER")) {
			// This condition implies that
			// if the carpet is NOT_EMITTER, solving will CONTINUE with the
			// actual
			// block surface the player is walking on
			// > NOT_EMITTER carpets will not cause solving to skip

			// Not a carpet
			association = isolator.getBlockMap().getBlockMaterial(state);

			if (association != null && !association.equals("NOT_EMITTER")) {
				// This condition implies that
				// foliage over a NOT_EMITTER block CANNOT PLAY

				// This block most not be executed if the association is a
				// carpet
				// => this block of code is here, not outside this if else
				// group.

				String foliage = isolator.getBlockMap().getBlockMapSubstrate(above, "foliage");
				if (foliage != null && !foliage.equals("NOT_EMITTER")) {
					association = association + "," + foliage;
					PFLog.debug("Foliage detected: " + foliage);
				}
			}
			// else { the information is discarded anyways, the method returns
			// null or no association }
		} else {
			yy = yy + 1;
			state = above;
			PFLog.debug("Carpet detected: " + association);
		}

		if (association != null) {
			if (association.equals("NOT_EMITTER")) {
				// Player has stepped on a non-emitter block
				// as defined in the blockmap

				// air block
				if (state.getBlock() != Blocks.air) {
					PFLog.debug("Not emitter for " + state.getBlock() + ":" + state.getProperties().toString());
				}

				return null;
			} else {
				PFLog.debug("Found association for " + state.getBlock() + ":" + state.getProperties().toString() + ": " + association);
				return (new Association(state, xx, yy, zz)).setAssociation(association);
			}
		} else {
			Association primitive = resolvePrimitive(state);
			if (primitive != null) {
				if (primitive.equals("NOT_EMITTER")) {
					PFLog.debug("Primitive for " + state.getBlock() + ":" + state.getProperties().toString() + ": " + primitive + " is NOT_EMITTER! Following behavior is uncertain.");
					return null;
				}

				PFLog.debug("Found primitive for " + state.getBlock() + ":" + state.getProperties().toString() + ": " + primitive);
				return primitive.init(state, xx, yy, zz);
			} else {
				PFLog.debug("No association for " + state.getBlock() + ":" + state.getProperties().toString());
				return (new Association(state, xx, yy, zz)).setNoAssociation();
			}
		}
	}

	private Association resolvePrimitive(IBlockState state) {
		// air block
		if (state.getBlock() == Blocks.air) return new Association("NOT_EMITTER");

		// impossible case?
		/*
		 * Block registered = Block.blocksList[block];
		 * 
		 * if (registered == null) return null;
		 */
		Block registered = state.getBlock();

		//

		// Check is the block is pass-through

		//

		// stepSound <= field_149762_H
		if (registered.stepSound == null) return new Association("NOT_EMITTER"); // This could return "" for empty sound, but let the engine try other things

		// stepSound.stepSoundName
		String soundName = registered.stepSound.soundName;
		if (soundName == null || soundName.isEmpty()) soundName = "UNDEFINED";

		// stepSound.stepSoundVolume stepSound.stepSoundPitch
		String substrate = String.format(Locale.ENGLISH, "%.2f_%.2f", registered.stepSound.volume, registered.stepSound.frequency);
		String primitive = isolator.getPrimitiveMap().getPrimitiveMapSubstrate(soundName, substrate);
		if (primitive == null) {
			// FIXME 2014-01-03 should fix that, it helps detecting glass
			// primitives
			/*
			 * primitive =
			 * isolator.getPrimitiveMap().getPrimitiveMapSubstrate(soundName,
			 * "break_" + registered.stepSound.getBreakSound());
			 * 
			 * if (primitive == null)
			 */primitive = isolator.getPrimitiveMap() .getPrimitiveMap(soundName);
		}

		if (primitive != null) {
			PFLog.debug("Primitive found for " + soundName + ":" + substrate);
			return (new Association()).setPrimative(primitive);
		} else {
			PFLog.debug("No primitive for " + soundName + ":" + substrate);
			return null;
		}
	}

	@Override
	public boolean playSpecialStoppingConditions(EntityPlayer ply) {
		if (ply.isInWater()) {
			float volume = MathHelper.sqrt_double(ply.motionX * ply.motionX * 0.2d + ply.motionY * ply.motionY + ply.motionZ * ply.motionZ * 0.2d) * 0.35f;
			ConfigOptions options = new ConfigOptions();
			options.getMap().put("gliding_volume", volume > 1 ? 1 : volume);
			// material water, see EntityLivingBase line 286
			isolator.getAcoustics().playAcoustic(ply, "_SWIM", ply.isInsideOfMaterial(Material.water) ? EventType.SWIM : EventType.WALK, options);
			return true;
		}

		return false;
	}

	@Override
	public boolean hasSpecialStoppingConditions(EntityPlayer ply) {
		return ply.isInWater();
	}

	@Override
	public Association findAssociationForBlock(int xx, int yy, int zz,
			String strategy) {
		if (!strategy.equals("find_messy_foliage"))
			return null;

		World world = Minecraft.getMinecraft().theWorld;

		/*
		 * Block block = PF172Helper.getBlockAt(xx, yy, zz); int metadata =
		 * world.getBlockMetadata(xx, yy, zz); // air block if (block ==
		 * Blocks.field_150350_a) { //int mm = world.blockGetRenderType(xx, yy -
		 * 1, zz); // see Entity, line 885 int mm = PF172Helper.getBlockAt(xx,
		 * yy - 1, zz).func_149645_b();
		 * 
		 * if (mm == 11 || mm == 32 || mm == 21) { block =
		 * PF172Helper.getBlockAt(xx, yy - 1, zz); metadata =
		 * world.getBlockMetadata(xx, yy - 1, zz); } }
		 */

		IBlockState above = world.getBlockState(new BlockPos(xx, yy + 1, zz));

		String association = null;
		boolean found = false;
		// Try to see if the block above is a carpet...
		/*
		 * String association =
		 * this.isolator.getBlockMap().getBlockMapSubstrate(
		 * PF172Helper.nameOf(xblock), xmetadata, "carpet");
		 * 
		 * if (association == null || association.equals("NOT_EMITTER")) { //
		 * This condition implies that // if the carpet is NOT_EMITTER, solving
		 * will CONTINUE with the actual // block surface the player is walking
		 * on // > NOT_EMITTER carpets will not cause solving to skip
		 * 
		 * // Not a carpet association =
		 * this.isolator.getBlockMap().getBlockMap(PF172Helper.nameOf(block),
		 * metadata);
		 * 
		 * if (association != null && !association.equals("NOT_EMITTER")) { //
		 * This condition implies that // foliage over a NOT_EMITTER block
		 * CANNOT PLAY
		 * 
		 * // This block most not be executed if the association is a carpet //
		 * => this block of code is here, not outside this if else group.
		 */

		String foliage = isolator.getBlockMap().getBlockMapSubstrate(above, "foliage");
		if (foliage != null && !foliage.equals("NOT_EMITTER")) {
			// we discard the normal block association, and mark the foliage as
			// detected
			// association = association + "," + foliage;
			association = foliage;
			String isMessy = isolator.getBlockMap().getBlockMapSubstrate(above, "messy");
			
			if (isMessy != null && isMessy.equals("MESSY_GROUND")) found = true;
		}
		/*
		 * } // else { the information is discarded anyways, the method returns
		 * null or no association } } else // Is a carpet return null;
		 */
		
		if (found && association != null) {
			return association.contentEquals("NOT_EMITTER") ? null : (new Association()).setAssociation(association);
		}
		return null;
	}
}
