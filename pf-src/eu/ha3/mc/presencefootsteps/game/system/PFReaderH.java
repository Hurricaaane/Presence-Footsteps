package eu.ha3.mc.presencefootsteps.game.system;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import eu.ha3.mc.haddon.Utility;
import eu.ha3.mc.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.mc.presencefootsteps.mcpackage.implem.NormalVariator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Generator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Isolator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Variator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.VariatorSettable;

public class PFReaderH implements Generator, VariatorSettable {
	// Construct
	final protected Isolator mod;
	final protected Utility util;
	
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
	
	public PFReaderH(Isolator isolator, Utility util) {
		mod = isolator;
		this.util = util;
		
		VAR = new NormalVariator();
	}
	
	@Override
	public void setVariator(Variator variator) {
		if (variator instanceof NormalVariator) {
			VAR = (NormalVariator) variator;
		}
	}
	
	@Override
	public void generateFootsteps(EntityPlayer ply) {
		simulateFootsteps(ply);
		simulateAirborne(ply);
		simulateBrushes(ply);
	}
	
	protected boolean stoppedImmobile(float reference) {
		float diff = lastReference - reference;
		lastReference = reference;
		if (!isImmobile && diff == 0f) {
			timeImmobile = System.currentTimeMillis();
			isImmobile = true;
		} else if (isImmobile && diff != 0f) {
			isImmobile = false;
			return System.currentTimeMillis() - timeImmobile > VAR.IMMOBILE_DURATION;
		}
		
		return false;
	}
	
	protected void simulateFootsteps(EntityPlayer ply) {
		final float distanceReference = ply.distanceWalkedOnStepModified;
		
		stepThisFrame = false;
		
		if (dmwBase > distanceReference) {
			dmwBase = 0;
			dwmYChange = 0;
		}
		
		double movX = ply.motionX;
		double movZ = ply.motionZ;
		
		double scal = movX * xMovec + movZ * zMovec;
		if (scalStat != scal < 0.001f) {
			scalStat = !scalStat;
			
			if (scalStat && VAR.PLAY_WANDER && !mod.getSolver().hasSpecialStoppingConditions(ply)) {
				mod.getSolver().playAssociation(ply, mod.getSolver().findAssociationForPlayer(ply, 0d, isRightFoot), EventType.WANDER);
			}
		}
		xMovec = movX;
		zMovec = movZ;
		
		if (ply.onGround || ply.isInWater() || ply.isOnLadder()) {
			EventType event = null;
			
			float dwm = distanceReference - dmwBase;
			boolean immobile = stoppedImmobile(distanceReference);
			if (immobile && !ply.isOnLadder()) {
				dwm = 0;
				dmwBase = distanceReference;
			}
			
			float distance = 0f;
			double verticalOffsetAsMinus = 0f;
			
			if (ply.isOnLadder() && !ply.onGround) {
				distance = VAR.DISTANCE_LADDER;
			} else if (!ply.isInWater() && Math.abs(this.yPosition - ply.posY) > 0.4d //&& Math.abs(this.yPosition - ply.posY) < 0.7d)
					) {
				// This ensures this does not get recorded as landing, but as a step
				if (yPosition < ply.posY) { // Going upstairs
					distance = this.VAR.DISTANCE_STAIR;
					event = speedDisambiguator(ply, EventType.UP, EventType.UP_RUN);
				} else if (!ply.isSneaking()) { // Going downstairs
					distance = -1f;
					verticalOffsetAsMinus = 0f;
					event = speedDisambiguator(ply, EventType.DOWN, EventType.DOWN_RUN);
				}
				
				dwmYChange = distanceReference;
				
			} else {
				distance = this.VAR.DISTANCE_HUMAN;
			}
			
			if (event == null) {
				event = speedDisambiguator(ply, EventType.WALK, EventType.RUN);
			}
			distance = reevaluateDistance(event, distance);
			
			if (dwm > distance) {
				produceStep(ply, event, verticalOffsetAsMinus);
				stepped(ply, event);
				this.dmwBase = distanceReference;
			}
		}
		
		if (ply.onGround) { // This fixes an issue where the value is evaluated while the player is between two steps in the air while descending stairs
			yPosition = ply.posY;
		}
	}
	
	protected void produceStep(EntityPlayer ply, EventType event) {
		produceStep(ply, event, 0d);
	}
	
	protected void produceStep(EntityPlayer ply, EventType event, double verticalOffsetAsMinus) {
		if (!mod.getSolver().playSpecialStoppingConditions(ply)) {
			if (event == null) event = speedDisambiguator(ply, EventType.WALK, EventType.RUN);
			mod.getSolver().playAssociation(ply, mod.getSolver().findAssociationForPlayer(ply, verticalOffsetAsMinus, isRightFoot), event);
			isRightFoot = !isRightFoot;
		}
		
		stepThisFrame = true;
	}
	
	protected void stepped(EntityPlayer ply, EventType event) {}
	
	protected float reevaluateDistance(EventType event, float distance) {
		return distance;
	}
	
	protected void simulateAirborne(EntityPlayer ply) {
		if ((ply.onGround || ply.isOnLadder()) == isFlying) {
			isFlying = !isFlying;
			simulateJumpingLanding(ply);
		}
		
		if (isFlying) fallDistance = ply.fallDistance;
	}
	
	protected void simulateJumpingLanding(EntityPlayer ply) {
		if (mod.getSolver().hasSpecialStoppingConditions(ply)) return;
		
		boolean isJumping;
		try {
			isJumping = (boolean)util.getPrivate(ply, "isJumping");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		if (isFlying && isJumping) { //ply.isJumping)
			if (VAR.EVENT_ON_JUMP) {
				double speed = ply.motionX * ply.motionX + ply.motionZ * ply.motionZ;
				
				if (speed < VAR.SPEED_TO_JUMP_AS_MULTIFOOT) { // STILL JUMP
					playMultifoot(ply, 0.4d, EventType.JUMP); // 2 - 0.7531999805212d (magic number for vertical offset?)
				} else {
					playSinglefoot(ply, 0.4d, EventType.JUMP, isRightFoot); // RUNNING JUMP
					// Do not toggle foot: After landing sounds, the first foot will be same as the one used to jump.
				}
			}
		} else if (!isFlying) {
			if (fallDistance > VAR.LAND_HARD_DISTANCE_MIN) {
				playMultifoot(ply, 0d, EventType.LAND); // Always assume the player lands on their two feet
				// Do not toggle foot: After landing sounds, the first foot will be same as the one used to jump.
			} else if (!this.stepThisFrame && !ply.isSneaking()) {
				playSinglefoot(ply, 0d, speedDisambiguator(ply, EventType.CLIMB, EventType.CLIMB_RUN), isRightFoot);
				isRightFoot = !isRightFoot;
			}
			
		}
	}
	
	protected EventType speedDisambiguator(EntityPlayer ply, EventType walk, EventType run) {
		double velocity = ply.motionX * ply.motionX + ply.motionZ * ply.motionZ;
		return velocity > VAR.SPEED_TO_RUN ? run : walk;
	}
	
	private void simulateBrushes(EntityPlayer ply) {
		if (brushesTime > System.currentTimeMillis()) return;
		
		brushesTime = System.currentTimeMillis() + 100;
		
		if ((ply.motionX == 0d && ply.motionZ == 0d) || ply.isSneaking()) return;
		
		//if (true || ply.onGround || ply.isOnLadder())
		//{
		int yy = MathHelper.floor_double(ply.posY - 0.1d - ply.getYOffset() - (ply.onGround ? 0d : 0.25d));
		Association assos = mod.getSolver().findAssociationForBlock(MathHelper.floor_double(ply.posX), yy, MathHelper.floor_double(ply.posZ), "find_messy_foliage");
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
	
	protected void playSinglefoot(EntityPlayer ply, double verticalOffsetAsMinus, EventType eventType, boolean foot) {
		Association assos = mod.getSolver().findAssociationForPlayer(ply, verticalOffsetAsMinus, isRightFoot);
		mod.getSolver().playAssociation(ply, assos, eventType);
	}
	
	protected void playMultifoot(EntityPlayer ply, double verticalOffsetAsMinus, EventType eventType) {
		// STILL JUMP
		Association leftFoot = mod.getSolver().findAssociationForPlayer(ply, verticalOffsetAsMinus, false);
		Association rightFoot = mod.getSolver().findAssociationForPlayer(ply, verticalOffsetAsMinus, true);
		
		if (leftFoot != null && leftFoot.equals(rightFoot) && !leftFoot.getNoAssociation()) {
			rightFoot = null; // If the two feet solve to the same sound, except NO_ASSOCIATION, only play the sound once
		}
		
		mod.getSolver().playAssociation(ply, leftFoot, eventType);
		mod.getSolver().playAssociation(ply, rightFoot, eventType);
	}
	
	protected float scalex(float number, float min, float max) {
		float m = (number - min) / (max - min);
		return m < 0 ? 0 : m > 1 ? 1 : m;
		
	}
}
