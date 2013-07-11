package net.minecraft.src;

import java.util.Random;

import eu.ha3.mc.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.mc.presencefootsteps.engine.interfaces.Options;
import eu.ha3.mc.presencefootsteps.mcpackage.implem.NormalVariator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Generator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Isolator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Variator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.VariatorSettable;

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

public class PFReaderH implements Generator, VariatorSettable
{
	// Construct
	final protected Isolator mod;
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
	
	public PFReaderH(Isolator isolator)
	{
		this.mod = isolator;
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
			
			if (ply.isOnLadder())
			{
				distance = this.VAR.LADDER_DISTANCE;
			}
			else if (Math.abs(this.yPosition - ply.posY) > 0.4d && Math.abs(this.yPosition - ply.posY) < 0.7d)
			{
				// This ensures this does not get recorded as landing, but as a step
				
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
				if (!this.mod.getSolver().playSpecialStoppingConditions(ply))
				{
					double speed = ply.motionX * ply.motionX + ply.motionZ * ply.motionZ;
					EventType event = speed > this.VAR.MODERN_SPEED_TO_RUN ? EventType.RUN : EventType.WALK;
					
					String assos = this.mod.getSolver().findAssociationForPlayer(ply, 0d, this.isRightFoot);
					this.mod.getSolver().playAssociation(ply, assos, event);
					
					this.isRightFoot = !this.isRightFoot;
				}
				
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
	
	protected void playAssociation(String association, EntityPlayer player, EventType event, Options options)
	{
		if (association == null)
			return;
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
		if (this.mod.getSolver().hasSpecialStoppingConditions(ply))
			return;
		
		if (this.isFlying && ply.isJumping)
		{
			if (this.VAR.MODERN_EVENT_ON_JUMP)
			{
				double speed = ply.motionX * ply.motionX + ply.motionZ * ply.motionZ;
				
				if (speed < this.VAR.MODERN_SPEED_TO_JUMP_AS_MULTIFOOT)
				{
					// STILL JUMP
					playMultifoot(ply, 0.5d, EventType.JUMP);
				}
				else
				{
					// RUNNING JUMP
					playSinglefoot(ply, 0.5d, EventType.JUMP, this.isRightFoot);
					
					// Do not toggle foot:
					// After landing sounds, the first foot will be same as the one used to jump.
				}
			}
		}
		else if (!this.isFlying && this.fallDistance > this.VAR.LAND_HARD_DISTANCE_MIN)
		{
			if (this.VAR.PLAY_STEP_ON_LAND_HARD)
			{
				// Always assume the player lands on their two feet
				playMultifoot(ply, 0d, EventType.LAND);
				
				// Do not toggle foot:
				// After landing sounds, the first foot will be same as the one used to jump.
			}
		}
	}
	
	protected void playSinglefoot(EntityPlayer ply, double verticalOffsetAsMinus, EventType eventType, boolean foot)
	{
		String assos = this.mod.getSolver().findAssociationForPlayer(ply, verticalOffsetAsMinus, this.isRightFoot);
		this.mod.getSolver().playAssociation(ply, assos, eventType);
	}
	
	protected void playMultifoot(EntityPlayer ply, double verticalOffsetAsMinus, EventType eventType)
	{
		// STILL JUMP
		String leftFoot = this.mod.getSolver().findAssociationForPlayer(ply, verticalOffsetAsMinus, false);
		String rightFoot = this.mod.getSolver().findAssociationForPlayer(ply, verticalOffsetAsMinus, true);
		
		if (leftFoot != null && leftFoot.equals(rightFoot) && !leftFoot.startsWith(PFSolver.NO_ASSOCIATION))
		{
			// If the two feet solve to the same sound, except NO_ASSOCIATION,
			// only play the sound once
			rightFoot = null;
		}
		
		this.mod.getSolver().playAssociation(ply, leftFoot, eventType);
		this.mod.getSolver().playAssociation(ply, rightFoot, eventType);
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
