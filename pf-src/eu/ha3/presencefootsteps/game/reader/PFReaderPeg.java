package eu.ha3.presencefootsteps.game.reader;

import eu.ha3.mc.haddon.Utility;
import eu.ha3.presencefootsteps.engine.implem.ConfigOptions;
import eu.ha3.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.presencefootsteps.game.interfaces.Isolator;
import net.minecraft.entity.player.EntityPlayer;

public class PFReaderPeg extends PFReaderQuad {
	
	protected boolean isFalling = false;
	
	protected FlightState state = FlightState.IDLE;
	protected int flapMod = 0;
	private long lastTimeImmobile;
	protected long nextFlapTime;
	
	public PFReaderPeg(Isolator isolator, Utility utility) {
		super(isolator, utility);
	}
	
	public void generateFootsteps(EntityPlayer ply) {
		lastTimeImmobile = timeImmobile;
		super.generateFootsteps(ply);
	}
	
	protected void simulateAirborne(EntityPlayer ply) {
		isFalling = ply.motionY < -0.3;
		super.simulateAirborne(ply);
		if (isAirborne) simulateFlying(ply);
	}
	
	protected boolean updateState(double x, double y, double z, double strafe) {
		double smotionHor = x*x + z * z;
		float motionHor = (float)Math.sqrt(smotionHor);
		float motionFull = (float)Math.sqrt(motionHor + (y*y));
		FlightState result = FlightState.IDLE;
		if (motionHor > VAR.MIN_DASH_MOTION) {
			result = FlightState.DASHING;
		} else if (motionHor > VAR.MIN_COAST_MOTION && (float)Math.abs(y) < VAR.MIN_COAST_MOTION/20) {
			if (strafe > VAR.MIN_MOTION_Y) {
				result = FlightState.COASTING_STRAFING;
			} else {
				result = FlightState.COASTING;
			}
		} else if (motionHor > VAR.MIN_MOTION_HOR) {
			result = FlightState.FLYING;
		} else if (y < 0) {
			result = FlightState.DESCENDING;
		} else if ((float)y > VAR.MIN_MOTION_Y) {
			result = FlightState.ASCENDING;
		}
		boolean changed = result != state;
		state = result;
		return changed;
	}
	
	protected int getWingSpeed() {
		switch (state) {
			case COASTING:
				if (flapMod == 0) return VAR.WING_SPEED_COAST;
				return VAR.WING_SPEED_NORMAL * flapMod;
			case COASTING_STRAFING:
				return VAR.WING_SPEED_NORMAL * (1 + flapMod);
			case DASHING:
				return VAR.WING_SPEED_RAPID;
			case ASCENDING:
			case FLYING:
				return VAR.WING_SPEED_NORMAL;
			default:
				return VAR.WING_SPEED_IDLE;
		}
	}
	
	protected void simulateJumpingLanding(EntityPlayer ply) {
		final long now = System.currentTimeMillis();
		
		double xpd = ply.motionX * ply.motionX + ply.motionZ * ply.motionZ;
		float speed = (float) Math.sqrt(xpd);
		
		if (isAirborne) nextFlapTime = now + VAR.WING_JUMPING_REST_TIME;
		
		boolean hugeLanding = !isAirborne && fallDistance > VAR.HUGEFALL_LANDING_DISTANCE_MIN;
		boolean speedingJumpStateChange = speed > VAR.MIN_MOTION_HOR;
		
		if (hugeLanding || speedingJumpStateChange) {
			if (!isAirborne) {
				float volume = speedingJumpStateChange ? 2 : scalex(fallDistance, VAR.HUGEFALL_LANDING_DISTANCE_MIN, VAR.HUGEFALL_LANDING_DISTANCE_MAX);
				mod.getAcoustics().playAcoustic(ply, "_SWIFT", EventType.LAND, new ConfigOptions().withOption("gliding_volume", volume));
			} else {
				mod.getAcoustics().playAcoustic(ply, "_SWIFT", EventType.JUMP, null);
			}
		}
		if (hugeLanding) super.simulateJumpingLanding(ply);
	}
	
	protected void simulateFlying(EntityPlayer ply) {
		final long now = System.currentTimeMillis();
		
		if (updateState(ply.motionX, ply.motionY, ply.motionZ, ply.moveStrafing)) {
			nextFlapTime = now + VAR.FLIGHT_TRANSITION_TIME;
		}
		
		if (!ply.isInWater() && !isFalling && now > nextFlapTime) {
			nextFlapTime = now + getWingSpeed() + (ply.worldObj.rand.nextInt(100) - 50);
			flapMod = (flapMod + 1) % (1 + ply.worldObj.rand.nextInt(4));
			
			float volume = 1;
			long diffImmobile = now - lastTimeImmobile;
			if (diffImmobile > VAR.WING_IMMOBILE_FADE_START) {
				volume -= scalex(diffImmobile, VAR.WING_IMMOBILE_FADE_START, VAR.WING_IMMOBILE_FADE_START + VAR.WING_IMMOBILE_FADE_DURATION);
			}
			mod.getAcoustics().playAcoustic(ply, "_WING", EventType.WALK, new ConfigOptions().withOption("gliding_volume", volume));
		}
	}
	
	private enum FlightState {
		DASHING, COASTING, COASTING_STRAFING, FLYING, IDLE, ASCENDING, DESCENDING
	}
}
