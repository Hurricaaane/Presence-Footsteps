package net.minecraft.src;

import java.util.Random;

import eu.ha3.mc.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.mc.presencefootsteps.mcpackage.implem.NormalVariator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.VariableGenerator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Variator;

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

public class PFReaderH implements VariableGenerator
{
	// Construct
	final protected PFHaddon mod;
	protected NormalVariator VAR;
	protected Random rand;
	
	// Footsteps
	protected float dmwBase;
	protected float dwmYChange;
	protected double yPosition;
	
	// Airborne
	protected boolean isFlying;
	protected float fallDistance;
	
	protected float lastReference;
	protected boolean isImmobile;
	protected long timeImmobile;
	
	protected boolean isRightFoot;
	
	public PFReaderH(PFHaddon mod)
	{
		this.mod = mod;
		this.VAR = new NormalVariator();
		this.rand = new Random();
	}
	
	@Override
	public void setVariator(Variator variator)
	{
		if (!(variator instanceof NormalVariator))
			return;
		
		this.VAR = (NormalVariator) variator;
	}
	
	@Override
	public void generateFootsteps(EntityPlayer ply)
	{
		simulateFootsteps(ply);
		simulateAirborne(ply);
	}
	
	protected boolean stoppedImmobile(float reference)
	{
		float diff = this.lastReference - reference;
		this.lastReference = reference;
		if (!this.isImmobile && diff == 0f)
		{
			this.timeImmobile = System.currentTimeMillis();
			this.isImmobile = true;
		}
		else if (this.isImmobile && diff != 0f)
		{
			this.isImmobile = false;
			long delay = System.currentTimeMillis() - this.timeImmobile;
			
			if (delay > this.VAR.IMMOBILE_DURATION)
				return true;
		}
		
		return false;
	}
	
	protected void simulateFootsteps(EntityPlayer ply)
	{
		final float distanceReference = ply.distanceWalkedOnStepModified;
		
		if (this.dmwBase > distanceReference)
		{
			this.dmwBase = 0;
			this.dwmYChange = 0;
		}
		
		if (ply.onGround || ply.isInWater() || ply.isOnLadder())
		{
			float dwm = distanceReference - this.dmwBase;
			boolean immobile = stoppedImmobile(distanceReference);
			
			float distance = 0f;
			float volume = this.VAR.WALK_VOLUME;
			
			if (ply.isOnLadder())
			{
				volume = this.VAR.LADDER_VOLUME;
				distance = this.VAR.LADDER_DISTANCE;
			}
			else if (Math.abs(this.yPosition - ply.posY) > 0.4d && Math.abs(this.yPosition - ply.posY) < 0.7d)
			{
				// This ensures this does not get recorded as landing, but as a step
				
				volume = this.VAR.STAIRCASE_VOLUME;
				// Going upstairs --- Going downstairs
				distance = this.yPosition < ply.posY ? this.VAR.HUMAN_DISTANCE * 0.65f : -1f;
				
				this.dwmYChange = distanceReference;
				
			}
			else
			{
				distance = this.VAR.HUMAN_DISTANCE;
			}
			
			if (immobile || dwm > distance)
			{
				volume = volume * this.VAR.GLOBAL_VOLUME_MULTIPLICATOR;
				
				double speed = ply.motionX * ply.motionX + ply.motionZ * ply.motionZ;
				
				makeSoundForPlayerBlock(ply, 0d, speed > 0.022f ? EventType.RUN : EventType.WALK);
				
				this.isRightFoot = !this.isRightFoot;
				this.dmwBase = distanceReference;
			}
		}
		
		if (ply.onGround)
		{
			// This fixes an issue where the value is evaluated
			// while the player is between two steps in the air
			// while descending stairs
			this.yPosition = ply.posY;
		}
		
	}
	
	protected void simulateAirborne(EntityPlayer ply)
	{
		if ((ply.onGround || ply.isOnLadder()) == this.isFlying)
		{
			this.isFlying = !this.isFlying;
			
			simulateJumpingLanding(ply);
		}
		
		if (this.isFlying)
		{
			this.fallDistance = ply.fallDistance;
		}
		
	}
	
	protected void simulateJumpingLanding(EntityPlayer ply)
	{
		if (this.isFlying && ply.isJumping)
		{
			if (this.VAR.PLAY_STEP_ON_JUMP)
			{
				makeSoundForPlayerBlock(ply, 0.5d, EventType.JUMP);
			}
		}
		else if (!this.isFlying && this.fallDistance > this.VAR.LAND_HARD_DISTANCE_MIN)
		{
			if (this.VAR.PLAY_STEP_ON_LAND_HARD)
			{
				makeSoundForPlayerBlock(ply, 0d, EventType.LAND);
			}
		}
	}
	
	protected void makeSoundForPlayerBlock(EntityPlayer ply, double minus, EventType event)
	{
		int yy = MathHelper.floor_double(ply.posY - 0.1d - ply.yOffset - minus); // Support for trapdoors
		//int yy = MathHelper.floor_double(ply.posY - 0.2d - ply.yOffset - minus);
		
		int xx;
		int zz;
		if (this.VAR.USE_TWO_FEET_DETECTION)
		{
			double rot = Math.toRadians(MathHelper.wrapAngleTo180_float(ply.rotationYaw));
			double xn = Math.cos(rot);
			double zn = Math.sin(rot);
			
			float feetDistanceToCenter = 0.2f * (this.isRightFoot ? -1 : 1);
			
			xx = MathHelper.floor_double(ply.posX + xn * feetDistanceToCenter);
			zz = MathHelper.floor_double(ply.posZ + zn * feetDistanceToCenter);
		}
		else
		{
			xx = MathHelper.floor_double(ply.posX);
			zz = MathHelper.floor_double(ply.posZ);
		}
		
		boolean worked = makeSoundForBlock(ply, xx, yy, zz, event);
		
		// If it didn't work, the player has walked over the air on the border of a block.
		// ------ ------  --> z
		//       | o    | < player is here
		//  wool | air  |
		// ------ ------
		//       |
		//       V z
		if (!worked)
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
						worked = makeSoundForBlock(ply, xx + 1, yy, zz, event);
					}
					else
					{
						worked = makeSoundForBlock(ply, xx - 1, yy, zz, event);
					}
				}
				else
				{
					if (zdang > 0)
					{
						worked = makeSoundForBlock(ply, xx, yy, zz + 1, event);
					}
					else
					{
						worked = makeSoundForBlock(ply, xx, yy, zz - 1, event);
					}
				}
				
				// If that didn't work, then maybe the footstep hit in the direction of walking
				// Try with the other closest block
				if (!worked)
				{
					// Take the maximum direction and try with the orthogonal direction of it
					if (isXdangMax)
					{
						if (zdang > 0)
						{
							worked = makeSoundForBlock(ply, xx, yy, zz + 1, event);
						}
						else
						{
							worked = makeSoundForBlock(ply, xx, yy, zz - 1, event);
						}
					}
					else
					{
						if (xdang > 0)
						{
							worked = makeSoundForBlock(ply, xx + 1, yy, zz, event);
						}
						else
						{
							worked = makeSoundForBlock(ply, xx - 1, yy, zz, event);
						}
					}
				}
				
			}
		}
	}
	
	protected boolean makeSoundForBlock(EntityPlayer ply, int xx, int yy, int zz, EventType event)
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
		
		if (block == 0)
			return false;
		
		if (ply.isInWater())
		{
			float var39 =
				MathHelper.sqrt_double(ply.motionX
					* ply.motionX * 0.2d + ply.motionY * ply.motionY + ply.motionZ * ply.motionZ * 0.2d) * 0.35f;
			
			if (var39 > 1.0F)
			{
				var39 = 1.0F;
			}
			
			ply.playSound("liquid.swim", var39, 1.0f + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4f);
		}
		else
		{
			// Try to see if the block above is a carpet...
			String association =
				this.mod.getAssociationForCarpet(
					world.getBlockId(xx, yy + 1, zz), world.getBlockMetadata(xx, yy + 1, zz));
			
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
				// Player has stepped on a non-emitter block by blockmap choice
				if (association.equals("NOT_EMITTER"))
				{
					PFHaddon.debug("Not emitter for " + block + ":" + metadata);
					
					return false;
				}
				
				PFHaddon.debug("Playing acoustic " + association + " for " + block + ":" + metadata);
				if (this.mod.getAcoustics().hasAcoustic(association))
				{
					this.mod.getAcoustics().playAcoustic(ply, association, event);
				}
				else
				{
					PFHaddon.debug("Acoustic " + association + " is missing!");
				}
			}
			else
			{
				PFHaddon.debug("Playing base Minecaft step for " + block + ":" + metadata);
				this.mod.getAcoustics().playStep(ply, xx, yy, zz, block);
			}
			
		}
		
		return true;
	}
	
	protected float scalex(float number, float min, float range)
	{
		float m = (number - min) / range;
		if (m < 0f)
			return 0f;
		if (m > 1f)
			return 1f;
		
		return m;
		
	}
	
	protected float randomPitch(float base, float radius)
	{
		return base + this.rand.nextFloat() * radius * 2 - radius;
	}
}
