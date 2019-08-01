package eu.ha3.presencefootsteps.sound.generator;

import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;

import eu.ha3.presencefootsteps.config.Variator;
import eu.ha3.presencefootsteps.mixins.ILivingEntity;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticLibrary;
import eu.ha3.presencefootsteps.util.PlayerUtil;
import eu.ha3.presencefootsteps.sound.Isolator;
import eu.ha3.presencefootsteps.world.Association;
import eu.ha3.presencefootsteps.world.Solver;

class BipedalStepSoundGenerator implements StepSoundGenerator {

    private double lastX;
    private double lastY;
    private double lastZ;

    protected double motionX;
    protected double motionY;
    protected double motionZ;

    // Construct
    protected final Solver solver;
    protected final AcousticLibrary acoustics;
    protected final Variator variator;

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

    protected long immobilePlayback;
    protected int immobileInterval;

    protected boolean isRightFoot;

    protected double xMovec;
    protected double zMovec;
    protected boolean scalStat;

    private boolean stepThisFrame;

    private boolean isMessyFoliage;
    private long brushesTime;

    protected PlayerEntity clientPlayer;

    public BipedalStepSoundGenerator(Isolator isolator) {
        solver = isolator.getSolver();
        acoustics = isolator.getAcoustics();
        variator = isolator.getVariator();
    }

    @Override
    public void generateFootsteps(LivingEntity ply) {
        simulateMotionData(ply);
        simulateFootsteps(ply);
        simulateAirborne(ply);
        simulateBrushes(ply);
        simulateStationary(ply);
    }

    protected void simulateStationary(LivingEntity ply) {
        if (isImmobile && (ply.onGround || !ply.isInWater()) && playbackImmobile()) {
            solver.playAssociation(ply, solver.findAssociation(ply, 0d, isRightFoot),
                    State.STAND);
        }
    }

    protected boolean playbackImmobile() {
        long now = System.currentTimeMillis();
        if (now - immobilePlayback > immobileInterval) {
            immobilePlayback = now;
            immobileInterval = (int) Math.floor(
                    (Math.random() * (variator.IMOBILE_INTERVAL_MAX - variator.IMOBILE_INTERVAL_MIN)) + variator.IMOBILE_INTERVAL_MIN);
            return true;
        }
        return false;
    }

    /**
     * Fills in the blanks that aren't present on the client when playing on a
     * remote server.
     */
    protected void simulateMotionData(LivingEntity ply) {
        clientPlayer = PlayerUtil.resolveToClientPlayer(ply);

        if (PlayerUtil.isClientPlayer(ply)) {
            motionX = clientPlayer.getVelocity().x;
            motionY = clientPlayer.getVelocity().y;
            motionZ = clientPlayer.getVelocity().z;
        } else {
            // Other players don't send their motion data so we have to make our own
            // approximations.
            motionX = (ply.x - lastX);
            lastX = ply.x;
            motionY = (ply.y - lastY);

            if (ply.onGround) {
                motionY += 0.0784000015258789d;
            }

            lastY = ply.y;
            motionZ = (ply.z - lastZ);
            lastZ = ply.z;
        }

        if (ply instanceof OtherClientPlayerEntity) {
            if (ply.world.getTime() % 1 == 0) {
                ply.distanceWalked += MathHelper.sqrt(motionX * motionX + motionZ * motionZ) * 0.8;

                if (motionX != 0 || motionZ != 0) {
                    ply.distanceWalked += MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ)
                            * 0.8;
                }

                if (ply.onGround) {
                    ply.fallDistance = 0;
                } else if (motionY < 0) {
                    ply.fallDistance -= motionY * 200;
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
            return System.currentTimeMillis() - timeImmobile > variator.IMMOBILE_DURATION;
        }

        return false;
    }

    protected void simulateFootsteps(LivingEntity ply) {
        final float distanceReference = ply.distanceWalked;

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

            if (scalStat && variator.PLAY_WANDER && !solver.hasStoppingConditions(ply)) {
                solver.playAssociation(ply, solver.findAssociation(ply, 0, isRightFoot),
                        State.WANDER);
            }
        }
        xMovec = movX;
        zMovec = movZ;

        if (ply.onGround || ply.isInWater() || ply.isClimbing()) {
            State event = null;

            float dwm = distanceReference - dmwBase;
            boolean immobile = stoppedImmobile(distanceReference);
            if (immobile && !ply.isClimbing()) {
                dwm = 0;
                dmwBase = distanceReference;
            }

            float distance = 0f;
            double verticalOffsetAsMinus = 0f;

            if (ply.isClimbing() && !ply.onGround) {
                distance = variator.DISTANCE_LADDER;
            } else if (!ply.isInWater() && Math.abs(yPosition - ply.y) > 0.4) {
                // This ensures this does not get recorded as landing, but as a step
                if (yPosition < ply.y) { // Going upstairs
                    distance = variator.DISTANCE_STAIR;
                    event = speedDisambiguator(ply, State.UP, State.UP_RUN);
                } else if (!ply.isSneaking()) { // Going downstairs
                    distance = -1f;
                    verticalOffsetAsMinus = 0f;
                    event = speedDisambiguator(ply, State.DOWN, State.DOWN_RUN);
                }

                dwmYChange = distanceReference;

            } else {
                distance = variator.DISTANCE_HUMAN;
            }

            if (event == null) {
                event = speedDisambiguator(ply, State.WALK, State.RUN);
            }
            distance = reevaluateDistance(event, distance);

            if (dwm > distance) {
                produceStep(ply, event, verticalOffsetAsMinus);
                stepped(ply, event);
                dmwBase = distanceReference;
            }
        }

        if (ply.onGround) {
            // This fixes an issue where the value is evaluated while the player is between
            // two steps in the air while descending stairs
            yPosition = ply.y;
        }
    }

    protected void produceStep(LivingEntity ply, State event) {
        produceStep(ply, event, 0d);
    }

    protected void produceStep(LivingEntity ply, @Nullable State event, double verticalOffsetAsMinus) {
        if (!solver.playStoppingConditions(ply)) {
            if (event == null) {
                event = speedDisambiguator(ply, State.WALK, State.RUN);
            }

            solver.playAssociation(ply, solver.findAssociation(ply, verticalOffsetAsMinus, isRightFoot), event);
            isRightFoot = !isRightFoot;
        }

        stepThisFrame = true;
    }

    protected void stepped(LivingEntity ply, State event) {

    }

    protected float reevaluateDistance(State event, float distance) {
        return distance;
    }

    protected void simulateAirborne(LivingEntity ply) {
        if ((ply.onGround || ply.isClimbing()) == isAirborne) {
            isAirborne = !isAirborne;
            simulateJumpingLanding(ply);
        }
        if (isAirborne) {
            fallDistance = ply.fallDistance;
        }
    }

    protected boolean isJumping(LivingEntity ply) {
        return ((ILivingEntity) ply).isJumping();
    }

    protected double getOffsetMinus(LivingEntity ply) {
        if (ply instanceof OtherClientPlayerEntity) {
            return 1;
        }
        return 0;
    }

    protected void simulateJumpingLanding(LivingEntity ply) {
        if (solver.hasStoppingConditions(ply)) {
            return;
        }

        if (isAirborne && isJumping(ply)) {
            simulateJumping(ply);
        } else if (!isAirborne) {
            simulateLanding(ply);
        }
    }

    protected void simulateJumping(LivingEntity ply) {
        if (variator.EVENT_ON_JUMP) {
            double speed = motionX * motionX + motionZ * motionZ;
            if (speed < variator.SPEED_TO_JUMP_AS_MULTIFOOT) {
                // STILL JUMP
                playMultifoot(ply, getOffsetMinus(ply) + 0.4d, State.JUMP);
                // 2 - 0.7531999805212d (magic number for vertical offset?)
            } else {
                playSinglefoot(ply, getOffsetMinus(ply) + 0.4d, State.JUMP, isRightFoot);
                // RUNNING JUMP
                // Do not toggle foot:
                // After landing sounds, the first foot will be same as the one used to jump.
            }
        }
    }

    protected void simulateLanding(LivingEntity ply) {
        if (fallDistance > variator.LAND_HARD_DISTANCE_MIN) {
            playMultifoot(ply, getOffsetMinus(ply), State.LAND);
            // Always assume the player lands on their two feet
            // Do not toggle foot:
            // After landing sounds, the first foot will be same as the one used to jump.
        } else if (/* !this.stepThisFrame && */!ply.isSneaking()) {
            playSinglefoot(ply, getOffsetMinus(ply), speedDisambiguator(ply, State.CLIMB, State.CLIMB_RUN),
                    isRightFoot);
            if (!this.stepThisFrame)
                isRightFoot = !isRightFoot;
        }
    }

    protected State speedDisambiguator(LivingEntity ply, State walk, State run) {
        if (!PlayerUtil.isClientPlayer(ply)) { // Other players don't send motion data, so have to decide some other way
            if (ply.isSprinting()) {
                return run;
            }
            return walk;
        }

        double velocity = motionX * motionX + motionZ * motionZ;
        return velocity > variator.SPEED_TO_RUN ? run : walk;
    }

    private void simulateBrushes(LivingEntity ply) {
        if (brushesTime > System.currentTimeMillis()) {
            return;
        }

        brushesTime = System.currentTimeMillis() + 100;

        if ((motionX == 0 && motionZ == 0) || ply.isSneaking()) {
            return;
        }

        Association assos = solver.findAssociation(ply.world, new BlockPos(
            ply.x,
            ply.y - 0.1D - ply.getHeightOffset() - (ply.onGround ? 0 : 0.25D),
            ply.z
        ), "find_messy_foliage");

        if (!assos.isNull()) {
            if (!isMessyFoliage) {
                isMessyFoliage = true;
                solver.playAssociation(ply, assos, State.WALK);
            }
        } else if (isMessyFoliage) {
            isMessyFoliage = false;
        }
    }

    protected void playSinglefoot(LivingEntity ply, double verticalOffsetAsMinus, State eventType, boolean foot) {
        Association assos = solver.findAssociation(ply, verticalOffsetAsMinus, isRightFoot);

        if (assos.isNotEmitter()) {
            assos = solver.findAssociation(ply, verticalOffsetAsMinus + 1, isRightFoot);
        }

        solver.playAssociation(ply, assos, eventType);
    }

    protected void playMultifoot(LivingEntity ply, double verticalOffsetAsMinus, State eventType) {
        // STILL JUMP
        Association leftFoot = solver.findAssociation(ply, verticalOffsetAsMinus, false);
        Association rightFoot = solver.findAssociation(ply, verticalOffsetAsMinus, true);

        if (leftFoot.hasAssociation() && leftFoot.equals(rightFoot)) {
            // If the two feet solve to the same sound, except NO_ASSOCIATION, only play the sound once
            rightFoot = Association.NOT_EMITTER;
        }

        solver.playAssociation(ply, leftFoot, eventType);
        solver.playAssociation(ply, rightFoot, eventType);
    }
}
