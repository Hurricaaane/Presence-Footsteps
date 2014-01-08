package eu.ha3.mc.presencefootsteps.game.system.deprecated;

import net.minecraft.entity.player.EntityPlayer;
import eu.ha3.mc.presencefootsteps.engine.implem.ConfigOptions;
import eu.ha3.mc.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Isolator;

/* x-placeholder-wtfplv2 */

@Deprecated
public class PFReader4P extends PFReaderH
{
	protected boolean isPegasus;
	
	// Hoofsteps
	protected int hoof;
	
	// Flying
	protected long airborneTime;
	protected long immobileTime;
	
	public PFReader4P(Isolator isolator)
	{
		super(isolator);
	}
	
	@Override
	public void generateFootsteps(EntityPlayer ply)
	{
		if (this.VAR.FORCE_HUMANOID)
		{
			simulateFootsteps(ply);
			simulateAirborne(ply);
			
			return;
		}
		
		simulateHoofsteps(ply);
		simulateFlying(ply);
	}
	
	protected void simulateFlying(EntityPlayer ply)
	{
		double xpd = ply.motionX * ply.motionX + ply.motionZ * ply.motionZ;
		float speed = (float) Math.sqrt(xpd);
		float volumetricSpeed = (float) Math.sqrt(xpd + ply.motionY * ply.motionY);
		
		if ((ply.onGround || ply.isOnLadder()) == this.isFlying)
		{
			this.isFlying = !this.isFlying;
			if (this.isFlying)
			{
				this.airborneTime = System.currentTimeMillis() + this.VAR.WING_JUMPING_REST_TIME;
			}
			
			boolean hugeLanding = !this.isFlying && this.fallDistance > this.VAR.HUGEFALL_LANDING_DISTANCE_MIN;
			boolean speedingJumpStateChange = speed > this.VAR.GROUND_AIR_STATE_SPEED;
			
			if (hugeLanding || speedingJumpStateChange)
			{
				if (!this.isFlying)
				{
					float volume =
						speedingJumpStateChange ? 1f : scalex(
							this.fallDistance, this.VAR.HUGEFALL_LANDING_DISTANCE_MIN,
							this.VAR.HUGEFALL_LANDING_DISTANCE_MAX);
					
					ConfigOptions options = new ConfigOptions();
					options.getMap().put("gliding_volume", volume);
					
					this.mod.getAcoustics().playAcoustic(ply, "_SWIFT", EventType.LAND, options);
				}
				else
				{
					this.mod.getAcoustics().playAcoustic(ply, "_SWIFT", EventType.JUMP);
				}
				
			}
			
			simulateJumpingLanding(ply);
		}
		
		// Fall distance is used by non-pegasi
		if (this.isFlying)
		{
			if (volumetricSpeed != 0)
			{
				this.immobileTime = System.currentTimeMillis();
			}
			this.fallDistance = ply.fallDistance;
		}
		
		// Only play wing sounds if pegasus
		if (!ply.isInWater() && this.isPegasus && this.isFlying && System.currentTimeMillis() > this.airborneTime)
		{
			int period = this.VAR.WING_SLOW;
			if (volumetricSpeed > this.VAR.WING_SPEED_MIN)
			{
				period =
					(int) (period - (this.VAR.WING_SLOW - this.VAR.WING_FAST)
						* scalex(volumetricSpeed, this.VAR.WING_SPEED_MIN, this.VAR.WING_SPEED_MAX));
			}
			
			this.airborneTime = System.currentTimeMillis() + period;
			
			float volume = 1f;
			long diffImmobile = System.currentTimeMillis() - this.immobileTime;
			if (System.currentTimeMillis() - this.immobileTime > this.VAR.WING_IMMOBILE_FADE_START)
			{
				volume =
					1f - scalex(diffImmobile, this.VAR.WING_IMMOBILE_FADE_START, this.VAR.WING_IMMOBILE_FADE_START
						+ this.VAR.WING_IMMOBILE_FADE_DURATION);
			}
			
			ConfigOptions options = new ConfigOptions();
			options.getMap().put("gliding_volume", volume);
			
			this.mod.getAcoustics().playAcoustic(ply, "_WING", EventType.WALK, options);
			
		}
		
	}
	
	protected void simulateHoofsteps(EntityPlayer ply)
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
			
			float speed = (float) Math.sqrt(ply.motionX * ply.motionX + ply.motionZ * ply.motionZ);
			float distance = 0f;
			
			boolean isGallop = false;
			@SuppressWarnings("unused")
			float specialVolume = -1f;
			
			EventType event = EventType.WALK;
			
			if (ply.isOnLadder())
			{
				distance = this.VAR.MODERN_DISTANCE_LADDER;
			}
			else if (Math.abs(this.yPosition - ply.posY) > 0.4d && Math.abs(this.yPosition - ply.posY) < 0.7d)
			{
				// This ensures this does not get recorded as landing, but as a step
				
				// Going upstairs --- Going downstairs
				distance = this.yPosition < ply.posY ? this.VAR.MODERN_DISTANCE_STAIR : -1f;
				
				// Regular stance on staircases (1-1-1-1-)
				
				this.dwmYChange = distanceReference;
				
			}
			else if (speed > this.VAR.SPEED_TO_GALLOP)
			{
				isGallop = true;
				// Gallop stance (1-1-2--)
				if (this.hoof == 3)
				{
					distance = this.VAR.GALLOP_DISTANCE_4;
				}
				else if (this.hoof == 2)
				{
					distance = this.VAR.GALLOP_DISTANCE_3;
				}
				else if (this.hoof == 1)
				{
					distance = this.VAR.GALLOP_DISTANCE_2;
				}
				else
				{
					distance = this.VAR.GALLOP_DISTANCE_1;
				}
				
				event = EventType.RUN;
			}
			else if (speed > this.VAR.SPEED_TO_WALK)
			{
				distance = this.VAR.WALK_DISTANCE;
				
				// Walking stance (2-2-)
				// Prevent the 2-2 steps from happening on staircases
				if (distanceReference - this.dwmYChange > this.VAR.STAIRCASE_ANTICHASE_DIFFERENCE)
				{
					if (this.hoof % 2 == 0)
					{
						//distance = distance / 7f;
						distance = distance * this.VAR.WALK_CHASING_FACTOR;
					}
				}
				
			}
			else
			{
				// Slow stance (1--1--1--1--)
				distance = this.VAR.SLOW_DISTANCE;
				specialVolume = this.VAR.SLOW_VOLUME * speed / this.VAR.SPEED_TO_WALK;
			}
			
			if (immobile || dwm > distance)
			{
				if (!this.mod.getSolver().playSpecialStoppingConditions(ply))
				{
					String assos = this.mod.getSolver().findAssociationForPlayer(ply, 0d, this.hoof % 2 == 0);
					this.mod.getSolver().playAssociation(ply, assos, event);
					
					if (isGallop && this.VAR.GALLOP_3STEP && this.hoof >= 2)
					{
						assos = this.mod.getSolver().findAssociationForPlayer(ply, 0d, false);
						this.mod.getSolver().playAssociation(ply, assos, event);
						
						this.hoof = 0;
					}
					else
					{
						this.hoof = (this.hoof + 1) % 4;
					}
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
}
