package eu.ha3.presencefootsteps.game;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import eu.ha3.presencefootsteps.engine.implem.ConfigOptions;
import eu.ha3.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.presencefootsteps.game.interfaces.Isolator;
import eu.ha3.presencefootsteps.game.interfaces.Solver;
import eu.ha3.presencefootsteps.log.PFLog;

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
	
	public PFSolver(Isolator isolator) {
		this.isolator = isolator;
	}
	
	@Override
	public void playAssociation(EntityPlayer ply, Association assos, EventType eventType) {
		if (assos != null && !assos.isNotEmitter()) {
			if (assos.getNoAssociation()) {
				isolator.getDefaultStepPlayer().playStep(ply, assos);
			} else {
				isolator.getAcoustics().playAcoustic(ply, assos, eventType);
			}
		}
	}
	
	@Override
	public Association findAssociationForPlayer(EntityPlayer ply, double verticalOffsetAsMinus, boolean isRightFoot) {
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
		int yy = MathHelper.floor_double(ply.posY - 0.1d - ply.getYOffset() - verticalOffsetAsMinus); // 0.1d: Support for trapdoors
		int xx = MathHelper.floor_double(ply.posX);
		int zz = MathHelper.floor_double(ply.posZ);
		return findAssociationForLocation(ply, xx, yy, zz);
	}

	@Override
	public Association findAssociationForLocation(EntityPlayer player, int x, int y, int z) {
		if (Math.abs(player.motionY) < 0.02) return null; // Don't play sounds on every tiny bounce
		if (player.isInWater()) PFLog.debug("WARNING!!! Playing a sound while in the water! This is supposed to be halted by the stopping conditions!!");
		
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
				if (isXdangMax) { // If we are in the positive border, add 1, else subtract 1
					worked = findAssociationForBlock(xdang > 0 ? x + 1 : x - 1, y, z);
				} else {
					worked = findAssociationForBlock(x, y, zdang > 0 ? z + 1 : z - 1);
				}

				// If that didn't work, then maybe the footstep hit in the
				// direction of walking
				// Try with the other closest block
				if (worked == null) { // Take the maximum direction and try with the orthogonal direction of it
					if (isXdangMax) {
						worked = findAssociationForBlock(x, y, zdang > 0 ? z + 1 : z - 1);
					} else {
						worked = findAssociationForBlock(xdang > 0 ? x + 1 : x - 1, y, z);
					}
				}
			}
		}
		return worked;
	}

	@Override
	public Association findAssociationForBlock(int xx, int yy, int zz) {
		World world = Minecraft.getMinecraft().theWorld;
		IBlockState in = world.getBlockState(new BlockPos(xx, yy, zz));
		
		IBlockState above = world.getBlockState(new BlockPos(xx, yy + 1, zz));
		String association = isolator.getBlockMap().getBlockMapSubstrate(above, "carpet"); // Try to see if the block above is a carpet...
		
		PFLog.debugf("Walking on block: %0 -- Being in block: %1", in.getBlock(), above.getBlock());

		if (association == null || association.equals("NOT_EMITTER")) {
			// This condition implies that if the carpet is NOT_EMITTER, solving will CONTINUE with the actual block surface the player is walking on
			// > NOT_EMITTER carpets will not cause solving to skip
			Material mat = in.getBlock().getMaterial();
			if (mat == Material.air || mat == Material.circuits) {
				IBlockState below = world.getBlockState(new BlockPos(xx, yy - 1, zz));
				association = isolator.getBlockMap().getBlockMapSubstrate(below, "bigger");
				if (association != null) {
					yy--;
					in = below;
					PFLog.debug("Fence detected: " + association);
				}
			}
			
			if (association == null) {
				association = isolator.getBlockMap().getBlockMap(in);
			}
			
			if (association != null && !association.equals("NOT_EMITTER")) {
				// This condition implies that foliage over a NOT_EMITTER block CANNOT PLAY
				// This block most not be executed if the association is a carpet
				// => this block of code is here, not outside this if else group.
				
				String foliage = isolator.getBlockMap().getBlockMapSubstrate(above, "foliage");
				if (foliage != null && !foliage.equals("NOT_EMITTER")) {
					association = association + "," + foliage;
					PFLog.debug("Foliage detected: " + foliage);
				}
			}
		} else {
			yy++;
			in = above;
			PFLog.debug("Carpet detected: " + association);
		}
		
		if (association != null) {
			if (association.contentEquals("NOT_EMITTER")) {
				if (in.getBlock() != Blocks.air) { // air block
					PFLog.debugf("Not emitter for %0 : %1", in);
				}
				return null; // Player has stepped on a non-emitter block as defined in the blockmap
			} else {
				PFLog.debugf("Found association for %0 : %1 : %2", in, association);
				return (new Association(in, xx, yy, zz)).setAssociation(association);
			}
		} else {
			String primitive = resolvePrimitive(in);
			if (primitive != null) {
				if (primitive.contentEquals("NOT_EMITTER")) {
					PFLog.debugf("Primitive for %0 : %1 : %2 is NOT_EMITTER! Following behavior is uncertain.", in, primitive);
					return null;
				}

				PFLog.debugf("Found primitive for %0 : %1 : %2", in, primitive);
				return (new Association(in, xx, yy, zz)).setPrimitive(primitive);
			} else {
				PFLog.debugf("No association for %0 : %1", in);
				return (new Association(in, xx, yy, zz)).setNoAssociation();
			}
		}
	}

	private String resolvePrimitive(IBlockState state) {
		Block block = state.getBlock();
		
		if (block == Blocks.air || block.stepSound == null) {
			return "NOT_EMITTER"; // air block
		}
		
		String soundName = block.stepSound.soundName;
		if (soundName == null || soundName.isEmpty()) {
			soundName = "UNDEFINED";
		}
		
		String substrate = String.format(Locale.ENGLISH, "%.2f_%.2f", block.stepSound.volume, block.stepSound.frequency);
		
		String primitive = isolator.getPrimitiveMap().getPrimitiveMapSubstrate(soundName, substrate); // Check for primitive in register 
		if (primitive == null) {
			if (block.stepSound.soundName != null) {
				primitive = isolator.getPrimitiveMap().getPrimitiveMapSubstrate(soundName, "break_" + soundName); // Check for break sound
			}
			if (primitive == null) {
				primitive = isolator.getPrimitiveMap().getPrimitiveMap(soundName);
			}
		}

		if (primitive != null) {
			PFLog.debug("Primitive found for " + soundName + ":" + substrate);
			return primitive;
		}
		
		PFLog.debug("No primitive for " + soundName + ":" + substrate);
		return null;
	}

	@Override
	public boolean playSpecialStoppingConditions(EntityPlayer ply) {
		if (ply.isInWater()) {
			float volume = MathHelper.sqrt_double(ply.motionX * ply.motionX * 0.2d + ply.motionY * ply.motionY + ply.motionZ * ply.motionZ * 0.2d) * 0.35f;
			ConfigOptions options = new ConfigOptions();
			options.getMap().put("gliding_volume", volume > 1 ? 1 : volume);
			// material water, see EntityLivingBase line 293
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
