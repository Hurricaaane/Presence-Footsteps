package eu.ha3.presencefootsteps.game.reader;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import eu.ha3.mc.haddon.Utility;
import eu.ha3.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.presencefootsteps.game.Association;
import eu.ha3.presencefootsteps.game.implem.NormalVariator;
import eu.ha3.presencefootsteps.game.interfaces.Generator;
import eu.ha3.presencefootsteps.game.interfaces.Isolator;
import eu.ha3.presencefootsteps.game.interfaces.Variator;
import eu.ha3.presencefootsteps.game.interfaces.VariatorSettable;

public class PFReader implements Generator, VariatorSettable {
	
	private double lastX;
	private double lastY;
	private double lastZ;
	
	protected double motionX;
	protected double motionY;
	protected double motionZ;
	
	// Construct
	final protected Isolator mod;
	final protected Utility util;
	
	protected NormalVariator VAR;
	
	// Footsteps
	protected float dmwBase;
	protected float dwmYChange;
	protected double yPosition;
	
	// Airborne
	protected boolean isAirborne;
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
	
	protected EntityPlayer clientPlayer;
	
	public PFReader(Isolator isolator, Utility util) {
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
		simulateMotionData(ply);
		simulateFootsteps(ply);
		simulateAirborne(ply);
		simulateBrushes(ply);
	}
	
	/**
	 * Fills in the blanks that aren't present on the client when playing on a remote server.
	 */
	protected void simulateMotionData(EntityPlayer ply) {
		clientPlayer = resolveToClientPlayer(ply);
		if (isClientPlayer(ply)) {
			EntityPlayer clientPlayer = util.getClient().getPlayer();
			motionX = clientPlayer.motionX;
			motionY = clientPlayer.motionY;
			motionZ = clientPlayer.motionZ;
		} else {
			//Other players don't send their motion data so we have to make out own approximations.
			//Note: This may change in 1.9. Watch this space!
			//Not perfect but it will have to suffice.
			motionX = (ply.posX - lastX);
			lastX = ply.posX;
			motionY = (ply.posY - lastY);
			if (ply.onGround) {
				motionY += 0.0784000015258789d;
			}
			lastY = ply.posY;
			motionZ = (ply.posZ - lastZ);
			lastZ = ply.posZ;
		}
		if (ply instanceof EntityOtherPlayerMP) {
			if (ply.worldObj.getWorldTime() % 1 == 0) {
				ply.distanceWalkedModified += (double)MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ) * 0.8D;
				if (motionX != 0 || motionZ != 0) {
					ply.distanceWalkedOnStepModified += (double)MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ) * 0.8D;
				}
				if (ply.onGround) {
					ply.fallDistance = 0;
				} else {
					if (motionY < 0) ply.fallDistance -= motionY * 200;
				}
			}
		}
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
		
		double movX = motionX;
		double movZ = motionZ;
		
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
			} else if (!ply.isInWater() && Math.abs(yPosition - ply.posY) > 0.4d
					//&& Math.abs(this.yPosition - ply.posY) < 0.7d)
					) {
				// This ensures this does not get recorded as landing, but as a step
				if (yPosition < ply.posY) { // Going upstairs
					distance = VAR.DISTANCE_STAIR;
					event = speedDisambiguator(ply, EventType.UP, EventType.UP_RUN);
				} else if (!ply.isSneaking()) { // Going downstairs
					distance = -1f;
					verticalOffsetAsMinus = 0f;
					event = speedDisambiguator(ply, EventType.DOWN, EventType.DOWN_RUN);
				}
				
				dwmYChange = distanceReference;
				
			} else {
				distance = VAR.DISTANCE_HUMAN;
			}
			
			if (event == null) {
				event = speedDisambiguator(ply, EventType.WALK, EventType.RUN);
			}
			distance = reevaluateDistance(event, distance);
			
			if (dwm > distance) {
				produceStep(ply, event, verticalOffsetAsMinus);
				stepped(ply, event);
				dmwBase = distanceReference;
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
		if ((ply.onGround || ply.isOnLadder()) == isAirborne) {
			isAirborne = !isAirborne;
			simulateJumpingLanding(ply);
		}
		if (isAirborne) fallDistance = ply.fallDistance;
	}
	
	protected boolean isJumping(EntityPlayer ply) {
		try {
			return (Boolean)util.getPrivate(clientPlayer, "isJumping");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	protected double getOffsetMinus(EntityPlayer ply) {
		if (ply instanceof EntityOtherPlayerMP) {
			return 1;
		}
		return 0;
	}
	
	protected void simulateJumpingLanding(EntityPlayer ply) {
		if (mod.getSolver().hasSpecialStoppingConditions(ply)) return;
		
		boolean isJumping = isJumping(ply);
		if (isAirborne && isJumping) {
			simulateJumping(ply);
		} else if (!isAirborne) {
			simulateLanding(ply);
		}
	}
	
	protected void simulateJumping(EntityPlayer ply) {
		if (VAR.EVENT_ON_JUMP) {
			double speed = motionX * motionX + motionZ * motionZ;
			if (speed < VAR.SPEED_TO_JUMP_AS_MULTIFOOT) { // STILL JUMP
				playMultifoot(ply, getOffsetMinus(ply) + 0.4d, EventType.JUMP); // 2 - 0.7531999805212d (magic number for vertical offset?)
			} else {
				playSinglefoot(ply, getOffsetMinus(ply) + 0.4d, EventType.JUMP, isRightFoot); // RUNNING JUMP
				// Do not toggle foot: After landing sounds, the first foot will be same as the one used to jump.
			}
		}
	}
	
	protected void simulateLanding(EntityPlayer ply) {
		if (fallDistance > VAR.LAND_HARD_DISTANCE_MIN) {
			playMultifoot(ply, getOffsetMinus(ply), EventType.LAND); // Always assume the player lands on their two feet
			// Do not toggle foot: After landing sounds, the first foot will be same as the one used to jump.
		} else if (!this.stepThisFrame && !ply.isSneaking()) {
			playSinglefoot(ply, getOffsetMinus(ply), speedDisambiguator(ply, EventType.CLIMB, EventType.CLIMB_RUN), isRightFoot);
			isRightFoot = !isRightFoot;
		}
	}
	
	private boolean isClientPlayer(EntityPlayer ply) {
		EntityPlayer clientPlayer = util.getClient().getPlayer();
		return ply.getUniqueID().equals(clientPlayer.getUniqueID());
	}
	
	private EntityPlayer resolveToClientPlayer(EntityPlayer ply) {
		EntityPlayer clientPlayer = util.getClient().getPlayer();
		return ply.getUniqueID().equals(clientPlayer.getUniqueID()) ? clientPlayer : ply;
	}
	
	protected EventType speedDisambiguator(EntityPlayer ply, EventType walk, EventType run) {
		if (!isClientPlayer(ply)) { //Other players don't send motion data, so have to decide some other way
			if (ply.isSprinting()) {
				return run;
			}
			return walk;
		}
		
		double velocity = motionX * motionX + motionZ * motionZ;
		return velocity > VAR.SPEED_TO_RUN ? run : walk;
	}
	
	private void simulateBrushes(EntityPlayer ply) {
		if (brushesTime > System.currentTimeMillis()) return;
		
		brushesTime = System.currentTimeMillis() + 100;
		
		if ((motionX == 0d && motionZ == 0d) || ply.isSneaking()) return;
		
		int yy = MathHelper.floor_double(ply.posY - 0.1d - ply.getYOffset() - (ply.onGround ? 0d : 0.25d));
		Association assos = mod.getSolver().findAssociationForBlock(ply.worldObj, MathHelper.floor_double(ply.posX), yy, MathHelper.floor_double(ply.posZ), "find_messy_foliage");
		
		if (assos != null) {
			if (!isMessyFoliage) {
				isMessyFoliage = true;
				mod.getSolver().playAssociation(ply, assos, EventType.WALK);
			}
		} else if (isMessyFoliage) {
			isMessyFoliage = false;
		}
	}
	
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
