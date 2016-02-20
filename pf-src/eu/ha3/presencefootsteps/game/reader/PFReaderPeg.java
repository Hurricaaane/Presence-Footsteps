package eu.ha3.presencefootsteps.game.reader;

import eu.ha3.mc.haddon.Utility;
import eu.ha3.presencefootsteps.engine.implem.ConfigOptions;
import eu.ha3.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.presencefootsteps.game.interfaces.Isolator;
import eu.ha3.presencefootsteps.util.PFHelper;
import net.minecraft.entity.player.EntityPlayer;

public class PFReaderPeg extends PFReaderQuad {

	public PFReaderPeg(Isolator isolator, Utility utility) {
		super(isolator, utility);
	}
	
	protected boolean isPegasus = true;
	
	// Flying
	protected long airborneTime;
	protected long immobileTime;

	protected void simulateAirborne(EntityPlayer ply) {
		if (!PFHelper.isGamePaused(util)) {
			simulateFlying(ply);
		}
		super.simulateAirborne(ply);
	}
	
	protected void simulateFlying(EntityPlayer ply) {
		final long now = System.currentTimeMillis();
		
		double xpd = ply.motionX * ply.motionX + ply.motionZ * ply.motionZ;
		float speed = (float) Math.sqrt(xpd);
		
		if ((ply.onGround || ply.isOnLadder()) == isFlying) {
			isFlying = !isFlying;
			if (isFlying) {
				airborneTime = now + VAR.WING_JUMPING_REST_TIME;
			}
			
			boolean hugeLanding = !isFlying && fallDistance > VAR.HUGEFALL_LANDING_DISTANCE_MIN;
			boolean speedingJumpStateChange = speed > VAR.GROUND_AIR_STATE_SPEED;
			
			if (hugeLanding || speedingJumpStateChange) {
				if (!this.isFlying) {
					float volume = speedingJumpStateChange ? 1 : scalex(fallDistance, VAR.HUGEFALL_LANDING_DISTANCE_MIN, VAR.HUGEFALL_LANDING_DISTANCE_MAX);
					
					ConfigOptions options = new ConfigOptions();
					options.getMap().put("gliding_volume", volume);
					
					mod.getAcoustics().playAcoustic(ply, "_WING", EventType.LAND, options);//_SWIFT
				} else {
					mod.getAcoustics().playAcoustic(ply, "_WING", EventType.JUMP, null);//_SWIFT
				}
				
			}
			
			simulateJumpingLanding(ply);
		}
		
		// Only play wing sounds if pegasus
		if (!ply.isInWater() && isPegasus && isFlying && now > airborneTime) {
			int period = VAR.WING_SLOW;
			float volumetricSpeed = (float) Math.sqrt(xpd + ply.motionY * ply.motionY);
			
			if (volumetricSpeed > VAR.WING_SPEED_MIN) {
				period = (int) (period - (VAR.WING_SLOW - VAR.WING_FAST) * scalex(volumetricSpeed, VAR.WING_SPEED_MIN, VAR.WING_SPEED_MAX));
			}
			
			airborneTime = now + period;
			
			float volume = 1f;
			long diffImmobile = now - immobileTime;
			if (diffImmobile > VAR.WING_IMMOBILE_FADE_START) {
				volume -= scalex(diffImmobile, VAR.WING_IMMOBILE_FADE_START, VAR.WING_IMMOBILE_FADE_START + VAR.WING_IMMOBILE_FADE_DURATION);
			}
			
			ConfigOptions options = new ConfigOptions();
			options.getMap().put("gliding_volume", volume);
			
			mod.getAcoustics().playAcoustic(ply, "_WING", EventType.WALK, options);
		}
		
	}
}
