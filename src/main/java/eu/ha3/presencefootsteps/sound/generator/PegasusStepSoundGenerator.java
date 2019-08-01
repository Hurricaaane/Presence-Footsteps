package eu.ha3.presencefootsteps.sound.generator;

import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.util.MathUtil;
import eu.ha3.presencefootsteps.sound.Isolator;
import eu.ha3.presencefootsteps.sound.Options;
import net.minecraft.entity.player.PlayerEntity;

class PegasusStepSoundGenerator extends QuadrapedalStepSoundGenerator {

    protected boolean isFalling = false;

    protected FlightState state = FlightState.IDLE;
    protected int flapMod = 0;
    private long lastTimeImmobile;
    protected long nextFlapTime;

    public PegasusStepSoundGenerator(Isolator isolator) {
        super(isolator);
    }

    @Override
    public void generateFootsteps(PlayerEntity ply) {
        lastTimeImmobile = timeImmobile;
        super.generateFootsteps(ply);
    }

    @Override
    protected void simulateAirborne(PlayerEntity ply) {
        isFalling = motionY < -0.3;
        super.simulateAirborne(ply);
        if (isAirborne)
            simulateFlying(ply);
    }

    protected boolean updateState(double x, double y, double z, double strafe) {
        double smotionHor = x * x + z * z;
        float motionHor = (float) Math.sqrt(smotionHor);
        FlightState result = FlightState.IDLE;
        if (motionHor > VAR.MIN_DASH_MOTION) {
            result = FlightState.DASHING;
        } else if (motionHor > VAR.MIN_COAST_MOTION && (float) Math.abs(y) < VAR.MIN_COAST_MOTION / 20) {
            if (strafe > VAR.MIN_MOTION_Y) {
                result = FlightState.COASTING_STRAFING;
            } else {
                result = FlightState.COASTING;
            }
        } else if (motionHor > VAR.MIN_MOTION_HOR) {
            result = FlightState.FLYING;
        } else if (y < 0) {
            result = FlightState.DESCENDING;
        } else if ((float) y > VAR.MIN_MOTION_Y) {
            result = FlightState.ASCENDING;
        }
        boolean changed = result != state;
        state = result;
        return changed;
    }

    protected int getWingSpeed() {
        switch (state) {
        case COASTING:
            if (flapMod == 0)
                return VAR.WING_SPEED_COAST;
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

    @Override
    protected void simulateJumpingLanding(PlayerEntity ply) {
        if (isolator.getSolver().hasStoppingConditions(ply))
            return;

        final long now = System.currentTimeMillis();

        double xpd = motionX * motionX + motionZ * motionZ;
        float speed = (float) Math.sqrt(xpd);

        if (isAirborne)
            nextFlapTime = now + VAR.WING_JUMPING_REST_TIME;

        boolean hugeLanding = !isAirborne && fallDistance > VAR.HUGEFALL_LANDING_DISTANCE_MIN;
        boolean speedingJumpStateChange = speed > VAR.MIN_MOTION_HOR;

        if (hugeLanding || speedingJumpStateChange) {
            if (!isAirborne) {
                float volume = speedingJumpStateChange ? 2
                        : MathUtil.scalex(fallDistance, VAR.HUGEFALL_LANDING_DISTANCE_MIN, VAR.HUGEFALL_LANDING_DISTANCE_MAX);
                isolator.getAcoustics().playAcoustic(ply, "_SWIFT", State.LAND,
                        Options.create().withOption("gliding_volume", volume));
            } else {
                isolator.getAcoustics().playAcoustic(ply, "_SWIFT", State.JUMP, null);
            }
        }

        boolean isJumping = isJumping(ply);

        if (isAirborne && isJumping) {
            simulateJumping(ply);
        } else if (!isAirborne && hugeLanding) {
            simulateLanding(ply);
        }
    }

    protected void simulateFlying(PlayerEntity ply) {
        final long now = System.currentTimeMillis();

        if (updateState(motionX, motionY, motionZ, ply.sidewaysSpeed)) {
            nextFlapTime = now + VAR.FLIGHT_TRANSITION_TIME;
        }

        if (!ply.isInWater() && !isFalling && now > nextFlapTime) {
            nextFlapTime = now + getWingSpeed() + (ply.world.random.nextInt(100) - 50);
            flapMod = (flapMod + 1) % (1 + ply.world.random.nextInt(4));

            float volume = 1;
            long diffImmobile = now - lastTimeImmobile;
            if (diffImmobile > VAR.WING_IMMOBILE_FADE_START) {
                volume -= MathUtil.scalex(diffImmobile, VAR.WING_IMMOBILE_FADE_START,
                        VAR.WING_IMMOBILE_FADE_START + VAR.WING_IMMOBILE_FADE_DURATION);
            }
            isolator.getAcoustics().playAcoustic(ply, "_WING", State.WALK,
                    Options.create().withOption("gliding_volume", volume));
        }
    }

    private enum FlightState {
        DASHING,
        COASTING,
        COASTING_STRAFING,
        FLYING,
        IDLE,
        ASCENDING,
        DESCENDING
    }
}
