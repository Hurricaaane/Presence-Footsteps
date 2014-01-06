package eu.ha3.mc.presencefootsteps.game.system;

import net.minecraft.entity.PFAccessor_NetMinecraftEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import eu.ha3.mc.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.mc.presencefootsteps.mcpackage.implem.NormalVariator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Generator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Isolator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Variator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.VariatorSettable;

/* x-placeholder-wtfplv2 */

public class PFReaderH implements Generator, VariatorSettable
{
	// Construct
	final protected Isolator mod;
	protected NormalVariator VAR;
	
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
	
	protected double xMovec;
	protected double zMovec;
	protected boolean scalStat;
	private boolean stepThisFrame;
	
	private boolean isMessyFoliage;
	private long brushesTime;
	
	public PFReaderH(Isolator isolator)
	{
		this.mod = isolator;
		this.VAR = new NormalVariator();
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
		simulateBrushes(ply);
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
		
		this.stepThisFrame = false;
		
		if (this.dmwBase > distanceReference)
		{
			this.dmwBase = 0;
			this.dwmYChange = 0;
		}
		
		double movX = ply.motionX;
		double movZ = ply.motionZ;
		
		double scal = movX * this.xMovec + movZ * this.zMovec;
		if (this.scalStat != scal < 0.001f)
		{
			this.scalStat = !this.scalStat;
			
			if (this.scalStat && this.VAR.MODERN_PLAY_WANDER && !this.mod.getSolver().hasSpecialStoppingConditions(ply))
			{
				String assos = this.mod.getSolver().findAssociationForPlayer(ply, 0d, this.isRightFoot);
				this.mod.getSolver().playAssociation(ply, assos, EventType.WANDER);
			}
		}
		this.xMovec = movX;
		this.zMovec = movZ;
		
		if (ply.onGround || ply.isInWater() || ply.isOnLadder())
		{
			EventType event = null;
			
			float dwm = distanceReference - this.dmwBase;
			boolean immobile = stoppedImmobile(distanceReference);
			if (immobile && !ply.isOnLadder())
			{
				dwm = 0;
				this.dmwBase = distanceReference;
			}
			
			float distance = 0f;
			double verticalOffsetAsMinus = 0f;
			
			if (ply.isOnLadder() && !ply.onGround)
			{
				distance = this.VAR.MODERN_DISTANCE_LADDER;
			}
			else if (!ply.isInWater() && Math.abs(this.yPosition - ply.posY) > 0.4d //&& Math.abs(this.yPosition - ply.posY) < 0.7d)
			)
			{
				// This ensures this does not get recorded as landing, but as a step
				
				// Going upstairs --- Going downstairs
				if (this.yPosition < ply.posY)
				{
					// Going upstairs
					distance = this.VAR.MODERN_DISTANCE_STAIR;
					event = speedDisambiguator(ply, EventType.UP, EventType.UP_RUN);
				}
				else if (!ply.isSneaking())
				{
					// Going downstairs
					distance = -1f;
					verticalOffsetAsMinus = 1f;
					event = speedDisambiguator(ply, EventType.DOWN, EventType.DOWN_RUN);
				}
				
				this.dwmYChange = distanceReference;
				
			}
			else
			{
				distance = this.VAR.MODERN_DISTANCE_HUMAN;
			}
			
			if (event == null)
			{
				event = speedDisambiguator(ply, EventType.WALK, EventType.RUN);
			}
			distance = reevaluateDistance(event, distance);
			
			if (dwm > distance)
			{
				produceStep(ply, event, verticalOffsetAsMinus);
				
				stepped(ply, event);
				
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
	
	protected void produceStep(EntityPlayer ply, EventType event)
	{
		produceStep(ply, event, 0d);
	}
	
	protected void produceStep(EntityPlayer ply, EventType event, double verticalOffsetAsMinus)
	{
		if (!this.mod.getSolver().playSpecialStoppingConditions(ply))
		{
			if (event == null)
			{
				event = speedDisambiguator(ply, EventType.WALK, EventType.RUN);
			}
			
			System.out.println(ply.boundingBox.minY - 0.1d);
			String assos = this.mod.getSolver().findAssociationForPlayer(ply, verticalOffsetAsMinus, this.isRightFoot);
			this.mod.getSolver().playAssociation(ply, assos, event);
			
			this.isRightFoot = !this.isRightFoot;
		}
		
		this.stepThisFrame = true;
	}
	
	protected void stepped(EntityPlayer ply, EventType event)
	{
	}
	
	protected float reevaluateDistance(EventType event, float distance)
	{
		return distance;
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
		
		if (this.isFlying && PFAccessor_NetMinecraftEntity.getInstance().isJumping(ply)) //ply.isJumping)
		{
			if (this.VAR.MODERN_EVENT_ON_JUMP)
			{
				double speed = ply.motionX * ply.motionX + ply.motionZ * ply.motionZ;
				
				if (speed < this.VAR.MODERN_SPEED_TO_JUMP_AS_MULTIFOOT)
				{
					// STILL JUMP
					// 2 - 0.7531999805212d (magic number for vertical offset?)
					playMultifoot(ply, 0.4d, EventType.JUMP);
				}
				else
				{
					// RUNNING JUMP
					playSinglefoot(ply, 0.4d, EventType.JUMP, this.isRightFoot);
					
					// Do not toggle foot:
					// After landing sounds, the first foot will be same as the one used to jump.
				}
			}
		}
		else if (!this.isFlying)
		{
			if (this.fallDistance > this.VAR.LAND_HARD_DISTANCE_MIN)
			{
				// Always assume the player lands on their two feet
				playMultifoot(ply, 0d, EventType.LAND);
				
				// Do not toggle foot:
				// After landing sounds, the first foot will be same as the one used to jump.
			}
			else if (!this.stepThisFrame && !ply.isSneaking())
			{
				playSinglefoot(ply, 0d, speedDisambiguator(ply, EventType.CLIMB, EventType.CLIMB_RUN), this.isRightFoot);
				this.isRightFoot = !this.isRightFoot;
			}
			
		}
	}
	
	protected EventType speedDisambiguator(EntityPlayer ply, EventType walk, EventType run)
	{
		double speed = ply.motionX * ply.motionX + ply.motionZ * ply.motionZ;
		return speed > this.VAR.MODERN_SPEED_TO_RUN ? run : walk;
	}
	
	private void simulateBrushes(EntityPlayer ply)
	{
		if (this.brushesTime > System.currentTimeMillis())
			return;
		
		this.brushesTime = System.currentTimeMillis() + 100;
		
		if (ply.motionX == 0d && ply.motionZ == 0d)
			return;
		
		if (ply.isSneaking())
			return;
		
		//if (true || ply.onGround || ply.isOnLadder())
		//{
		int yy = MathHelper.floor_double(ply.posY - 0.1d - ply.yOffset - (ply.onGround ? 0d : 0.25d));
		String assos =
			this.mod.getSolver().findAssociationForBlock(
				MathHelper.floor_double(ply.posX), yy, MathHelper.floor_double(ply.posZ), "find_messy_foliage");
		if (assos != null)
		{
			if (!this.isMessyFoliage)
			{
				this.isMessyFoliage = true;
				this.mod.getSolver().playAssociation(ply, assos, EventType.WALK);
			}
		}
		else
		{
			if (this.isMessyFoliage)
			{
				this.isMessyFoliage = false;
			}
		}
		//}
	}
	
	// 
	
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
	
	protected float scalex(float number, float min, float max)
	{
		float m = (number - min) / (max - min);
		if (m < 0f)
			return 0f;
		if (m > 1f)
			return 1f;
		
		return m;
		
	}
}
