package net.minecraft.src;

import java.util.Random;

import eu.ha3.mc.presencefootsteps.interfaces.EventType;

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

public class PFReaderH implements PFGenerator
{
	// Construct
	final protected PFHaddon mod;
	protected PFVariator VAR;
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
	
	public PFReaderH(PFHaddon mod)
	{
		this.mod = mod;
		this.VAR = new PFVariator();
		this.rand = new Random();
	}
	
	@Override
	public void setVariator(PFVariator variator)
	{
		this.VAR = variator;
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
		// final float distanceReference = ply.field_82151_R;
		final float distanceReference = ply.distanceWalkedOnStepModified;
		//System.out.println(distanceReference);
		if (this.dmwBase > distanceReference)
		{
			this.dmwBase = 0;
			this.dwmYChange = 0;
		}
		
		if (ply.onGround || ply.isInWater() || ply.isOnLadder())
		{
			float dwm = distanceReference - this.dmwBase;
			boolean immobile = stoppedImmobile(distanceReference);
			
			//float speed = (float) Math.sqrt(ply.motionX * ply.motionX + ply.motionZ * ply.motionZ);
			float distance = 0f;
			float volume = this.VAR.WALK_VOLUME;
			
			if (ply.isOnLadder())
			{
				volume = this.VAR.LADDER_VOLUME;
				distance = this.VAR.LADDER_DISTANCE;
			}
			/*else if (!ply.onGround && !ply.isInWater())
			{
				volume = 0;
			}*/
			else if (Math.abs(this.yPosition - ply.posY) > 0.4d)
			{
				volume = this.VAR.STAIRCASE_VOLUME;
				distance = this.VAR.STAIRCASE_DISTANCE;
				this.dwmYChange = distanceReference;
				
			}
			else
			{
				distance = this.VAR.HUMAN_DISTANCE;
			}
			
			if (immobile || dwm > distance)
			{
				volume = volume * this.VAR.GLOBAL_VOLUME_MULTIPLICATOR;
				makeSoundForPlayerBlock(ply, volume, 0d, EventType.WALK);
				
				this.dmwBase = distanceReference;
			}
		}
		
		this.yPosition = ply.posY;
		
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
				makeSoundForPlayerBlock(
					ply, this.VAR.JUMP_VOLUME * this.VAR.GLOBAL_VOLUME_MULTIPLICATOR, 0.5d, EventType.JUMP);
			}
			
			if (this.VAR.PLAY_SPECIAL_ON_JUMP)
			{
				// MAKE VARIATOR FOR PITCH ETC.
				//ply.playSound("UNDONE", 0, 1.0f + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4f);
			}
		}
		else if (!this.isFlying && this.fallDistance > this.VAR.LAND_HARD_DISTANCE_MIN)
		{
			if (this.VAR.PLAY_STEP_ON_LAND_HARD)
			{
				makeSoundForPlayerBlock(
					ply, this.VAR.LAND_HARD_VOLUME * this.VAR.GLOBAL_VOLUME_MULTIPLICATOR, 0d, EventType.LAND);
			}
			
			if (this.VAR.PLAY_SPECIAL_ON_LAND_HARD)
			{
				// MAKE VARIATOR FOR PITCH ETC.
				//ply.playSound("UNDONE", 0, 1.0f + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4f);
			}
		}
	}
	
	protected void makeSoundForPlayerBlock(EntityPlayer ply, float volume, double minus, EventType event)
	{
		int xx = MathHelper.floor_double(ply.posX);
		int yy = MathHelper.floor_double(ply.posY - 0.1d - ply.yOffset - minus); // Support for trapdoors
		//int yy = MathHelper.floor_double(ply.posY - 0.2d - ply.yOffset - minus);
		int zz = MathHelper.floor_double(ply.posZ);
		
		boolean worked = makeSoundForBlock(ply, volume, xx, yy, zz, event);
		
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
						worked = makeSoundForBlock(ply, volume, xx + 1, yy, zz, event);
					}
					else
					{
						worked = makeSoundForBlock(ply, volume, xx - 1, yy, zz, event);
					}
				}
				else
				{
					if (zdang > 0)
					{
						worked = makeSoundForBlock(ply, volume, xx, yy, zz + 1, event);
					}
					else
					{
						worked = makeSoundForBlock(ply, volume, xx, yy, zz - 1, event);
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
							worked = makeSoundForBlock(ply, volume, xx, yy, zz + 1, event);
						}
						else
						{
							worked = makeSoundForBlock(ply, volume, xx, yy, zz - 1, event);
						}
					}
					else
					{
						if (xdang > 0)
						{
							worked = makeSoundForBlock(ply, volume, xx + 1, yy, zz, event);
						}
						else
						{
							worked = makeSoundForBlock(ply, volume, xx - 1, yy, zz, event);
						}
					}
				}
				
			}
		}
	}
	
	protected boolean makeSoundForBlock(EntityPlayer ply, float volume, int xx, int yy, int zz, EventType event)
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
		
		if (block > 0)
		{
			boolean overrode = false;
			
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
				volume = volume * this.VAR.MATSTEPS_VOLUME_MULTIPLICATOR;
				if (volume > 0)
				{
					if (this.VAR.PLAY_MATSTEPS)
					{
						String sound = this.mod.getSoundForBlock(block, metadata, event);
						String flak =
							this.mod.getFlakForBlock(
								world.getBlockId(xx, yy + 1, zz), world.getBlockMetadata(xx, yy + 1, zz), event);
						
						if (flak != null)
						{
							sound = flak;
							PFHaddon.debug("Flak enabled");
						}
						
						if (sound != null)
						{
							// Player has stepped on a non-emitter block by blockmap choice
							if (sound.equals("NOT_EMITTER"))
							{
								PFHaddon.debug("Not emitter for " + block + ":" + metadata);
								
								return false;
							}
							
							// Player has stepped on a non-blank sound
							if (!sound.equals("BLANK"))
							{
								//this.mod.manager().getMinecraft().theWorld.playSound(
								//	ply.posX, ply.posY, ply.posZ, sound, volume,
								//	randomPitch(1f, this.VAR.MATSTEP_PITCH_RADIUS), false);
								ply.playSound(sound, volume, randomPitch(1f, this.VAR.MATSTEP_PITCH_RADIUS));
								
								PFHaddon.debug("Playing sound " + sound + " for " + block + ":" + metadata);
								
							}
							else
							{
								PFHaddon.debug("Blank sound for " + block + ":" + metadata);
							}
						}
						
						// Sound isn't null
						if (sound != null /* && !sound.equals("DEFAULT")*/)
						{
							overrode = true;
						}
						else
						{
							PFHaddon.debug("Fallback to default for " + block + ":" + metadata);
						}
					}
					
					// Should it play blocksteps? Requires blocksteps option and (all overrides or not overridden)
					if (this.VAR.PLAY_BLOCKSTEPS && (!this.VAR.PLAY_OVERRIDES || !overrode))
					{
						ply.playStepSound(xx, yy, zz, block);
						PFHaddon.debug("Playing base Minecaft step for " + block);
					}
				}
			}
			
		}
		else
			return false;
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
