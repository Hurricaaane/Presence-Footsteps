package net.minecraft.src;

import eu.ha3.mc.presencefootsteps.engine.implem.ConfigOptions;
import eu.ha3.mc.presencefootsteps.engine.interfaces.EventType;

/*
            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
                    Version 2, December 2004 

 Copyright (C) 2004 Sam Hocevar <sam@hocevar.net> 

 Everyone is permitted to copy and distribute verbatim or modified 
 copies of this license document, and changing it is allowed as long 
 as the name is changed. 

            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION 

  0. You just DO WHAT THE FUCK YOU WANT TO. 
*/

/**
 * Solves in-world locations and players into associations. Associations are an
 * extension of Acoustic names, with some special codes. They are derived from
 * the blockmap and defined with these values:<br>
 * <br>
 * The association null is derived from blockmap "NOT_EMITTER" and means the
 * block is NOT MEANT to emit sounds (not equal to "no sound").<br>
 * The association "_NO_ASSOCIATION" is derived from AN ABSENCE of an entry in
 * the blockmap (after solving missing metadata and carpets).<br>
 * Any other association string returned by the findAssociation
 * 
 * 
 * @author Hurry
 * 
 */
public class PFSolver
{
	private final PFHaddon mod;
	
	public PFSolver(PFHaddon mod)
	{
		this.mod = mod;
	}
	
	/**
	 * Find an association for a player particular foot. This will fetch the
	 * player angle and use it as a basis to find out what block is below their
	 * feet (or which block is likely to be below their feet if the player is
	 * walking on the edge of a block when walking over non-emitting blocks like
	 * air or water).<br>
	 * <br>
	 * Returns null if no blocks are valid emitting blocks.<br>
	 * Returns "_NO_ASSOCIATION" if a matching block was found, but has no
	 * association in the blockmap.
	 * 
	 * @param ply
	 * @param verticalOffsetAsMinus
	 * @param isRightFoot
	 */
	public String findAssociationForPlayer(EntityPlayer ply, double verticalOffsetAsMinus, boolean isRightFoot)
	{
		int yy = MathHelper.floor_double(ply.posY - 0.1d - ply.yOffset - verticalOffsetAsMinus);
		// 0.1d: Support for trapdoors
		
		double rot = Math.toRadians(MathHelper.wrapAngleTo180_float(ply.rotationYaw));
		double xn = Math.cos(rot);
		double zn = Math.sin(rot);
		
		float feetDistanceToCenter = 0.2f * (isRightFoot ? -1 : 1);
		
		int xx = MathHelper.floor_double(ply.posX + xn * feetDistanceToCenter);
		int zz = MathHelper.floor_double(ply.posZ + zn * feetDistanceToCenter);
		
		return findAssociationFromLocation(ply, xx, yy, zz);
	}
	
	/**
	 * Find an association for a player. This will take the block right below
	 * the center of the player (or which block is likely to be below them if
	 * the player is walking on the edge of a block when walking over
	 * non-emitting blocks like air or water).<br>
	 * <br>
	 * Returns null if no blocks are valid emitting blocks.<br>
	 * Returns "_NO_ASSOCIATION" if a matching block was found, but has no
	 * association in the blockmap.
	 * 
	 * @param ply
	 * @param verticalOffsetAsMinus
	 * @param isRightFoot
	 */
	public String findAssociationForPlayer(EntityPlayer ply, double verticalOffsetAsMinus)
	{
		int yy = MathHelper.floor_double(ply.posY - 0.1d - ply.yOffset - verticalOffsetAsMinus);
		// 0.1d: Support for trapdoors
		
		int xx = MathHelper.floor_double(ply.posX);
		int zz = MathHelper.floor_double(ply.posZ);
		return findAssociationFromLocation(ply, xx, yy, zz);
	}
	
	/**
	 * Find an association for a player, and a location. This will try to find
	 * the best matching block on that location, or near that location, for
	 * instance if the player is walking on the edge of a block when walking
	 * over non-emitting blocks like air or water)<br>
	 * <br>
	 * Returns null if no blocks are valid emitting blocks.<br>
	 * Returns "_NO_ASSOCIATION" if a matching block was found, but has no
	 * association in the blockmap.
	 * 
	 * @param ply
	 * @param xx
	 * @param yy
	 * @param zz
	 * @return
	 */
	public String findAssociationFromLocation(EntityPlayer ply, int xx, int yy, int zz)
	{
		if (ply.isInWater())
		{
			PFHaddon.debug("WARNING!!! Playing a sound while in the water! "
				+ "This is supposed to be halted by the stopping conditions!!");
		}
		
		String worked = findAssociationForBlock(xx, yy, zz);
		
		// If it didn't work, the player has walked over the air on the border of a block.
		// ------ ------  --> z
		//       | o    | < player is here
		//  wool | air  |
		// ------ ------
		//       |
		//       V z
		if (worked == null)
		{
			// Create a trigo. mark contained inside the block the player is over
			double xdang = (ply.posX - xx) * 2 - 1;
			double zdang = (ply.posZ - zz) * 2 - 1;
			// -1   0   1
			//   -------  -1
			//  | o     |
			//  |   +   |  0 --> x
			//  |       |
			//   -------   1
			//      |
			//      V z
			
			// If the player is at the edge of that
			if (/*Math.sqrt(xdang * xdang + zdang * zdang) > 0.6*/Math.max(Math.abs(xdang), Math.abs(zdang)) > 0.2f)
			{
				// Find the maximum absolute value of X or Z
				boolean isXdangMax = Math.abs(xdang) > Math.abs(zdang);
				//  --------------------- ^ maxofZ-
				// |  .               .  |
				// |    .           .    |
				// |  o   .       .      |
				// |        .   .        |
				// |          .          |
				// < maxofX-     maxofX+ >
				
				// Take the maximum border to produce the sound
				if (isXdangMax)
				{
					// If we are in the positive border, add 1, else subtract 1
					if (xdang > 0)
					{
						worked = findAssociationForBlock(xx + 1, yy, zz);
					}
					else
					{
						worked = findAssociationForBlock(xx - 1, yy, zz);
					}
				}
				else
				{
					if (zdang > 0)
					{
						worked = findAssociationForBlock(xx, yy, zz + 1);
					}
					else
					{
						worked = findAssociationForBlock(xx, yy, zz - 1);
					}
				}
				
				// If that didn't work, then maybe the footstep hit in the direction of walking
				// Try with the other closest block
				if (worked == null)
				{
					// Take the maximum direction and try with the orthogonal direction of it
					if (isXdangMax)
					{
						if (zdang > 0)
						{
							worked = findAssociationForBlock(xx, yy, zz + 1);
						}
						else
						{
							worked = findAssociationForBlock(xx, yy, zz - 1);
						}
					}
					else
					{
						if (xdang > 0)
						{
							worked = findAssociationForBlock(xx + 1, yy, zz);
						}
						else
						{
							worked = findAssociationForBlock(xx - 1, yy, zz);
						}
					}
				}
			}
		}
		return worked;
	}
	
	/**
	 * Find an association for a certain block assuming the player is standing
	 * on it. This will sometimes select the block above because some block act
	 * like carpets. This also applies when the block targeted by the location
	 * is actually not emitting, such as lilypads on water.<br>
	 * <br>
	 * Returns null if the block is not a valid emitting block (this causes the
	 * engine to continue looking for valid blocks).<br>
	 * Returns "_NO_ASSOCIATION" if the block is valid, but has no association
	 * in the blockmap.
	 * 
	 * @param ply
	 * @param xx
	 * @param yy
	 * @param zz
	 * @return
	 */
	public String findAssociationForBlock(int xx, int yy, int zz)
	{
		World world = this.mod.manager().getMinecraft().theWorld;
		
		int block = world.getBlockId(xx, yy, zz);
		int metadata = world.getBlockMetadata(xx, yy, zz);
		if (block == 0)
		{
			int mm = world.blockGetRenderType(xx, yy - 1, zz);
			
			if (mm == 11 || mm == 32 || mm == 21)
			{
				block = world.getBlockId(xx, yy - 1, zz);
				metadata = world.getBlockMetadata(xx, yy - 1, zz);
			}
		}
		
		// If the block is air, it is not an emitter
		if (block == 0)
			return null;
		
		// Try to see if the block above is a carpet...
		String association =
			this.mod.getAssociationForCarpet(world.getBlockId(xx, yy + 1, zz), world.getBlockMetadata(xx, yy + 1, zz));
		
		if (association == null)
		{
			// Not a carpet
			association = this.mod.getAssociationForBlock(block, metadata);
		}
		else
		{
			PFHaddon.debug("Carpet detected");
		}
		
		if (association != null)
		{
			if (association.equals("NOT_EMITTER"))
			{
				// Player has stepped on a non-emitter block
				// as defined in the blockmap
				PFHaddon.debug("Not emitter for " + block + ":" + metadata);
				
				return null;
			}
			else
			{
				PFHaddon.debug("Found association for " + block + ":" + metadata + ": " + association);
				return association;
			}
		}
		else
		{
			PFHaddon.debug("No association for " + block + ":" + metadata);
			return "_NO_ASSOCIATION";
		}
	}
	
	/**
	 * Play special sounds that must stop the usual footstep figuring things out
	 * process.
	 * 
	 * @param ply
	 */
	public boolean playSpecialStoppingConditions(EntityPlayer ply)
	{
		if (ply.isInWater())
		{
			float volume =
				MathHelper.sqrt_double(ply.motionX
					* ply.motionX * 0.2d + ply.motionY * ply.motionY + ply.motionZ * ply.motionZ * 0.2d) * 0.35f;
			
			if (volume > 1.0F)
			{
				volume = 1.0F;
			}
			
			ConfigOptions options = new ConfigOptions();
			options.getMap().put("gliding_volume", volume);
			
			this.mod.getAcoustics().playAcoustic(ply, "_SWIM", EventType.SWIM, options);
			
			return true;
		}
		
		return false;
	}
}
