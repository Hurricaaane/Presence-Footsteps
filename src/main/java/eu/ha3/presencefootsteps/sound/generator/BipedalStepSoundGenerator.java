package eu.ha3.presencefootsteps.sound.generator;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import eu.ha3.presencefootsteps.config.Variator;
import eu.ha3.presencefootsteps.mixins.ILivingEntity;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.Isolator;
import eu.ha3.presencefootsteps.world.Association;

class BipedalStepSoundGenerator implements StepSoundGenerator {

    private double lastX;
    private double lastY;
    private double lastZ;

    protected double motionX;
    protected double motionY;
    protected double motionZ;

    // Construct
    final protected Isolator isolator;

    protected Variator VAR;

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
        this.isolator = isolator;
        VAR = isolator.getVariator();
    }

    @Override
    public void generateFootsteps(PlayerEntity ply) {
        simulateMotionData(ply);
        simulateFootsteps(ply);
        simulateAirborne(ply);
        simulateBrushes(ply);
        simulateStationary(ply);
    }

    protected void simulateStationary(PlayerEntity ply) {
        if (isImmobile && (ply.onGround || !ply.isInWater()) && playbackImmobile()) {
            isolator.getSolver().playAssociation(ply, isolator.getSolver().findAssociation(ply, 0d, isRightFoot),
                    State.STAND);
        }
    }

    protected boolean playbackImmobile() {
        long now = System.currentTimeMillis();
        if (now - immobilePlayback > immobileInterval) {
            immobilePlayback = now;
            immobileInterval = (int) Math.floor(
                    (Math.random() * (VAR.IMOBILE_INTERVAL_MAX - VAR.IMOBILE_INTERVAL_MIN)) + VAR.IMOBILE_INTERVAL_MIN);
            return true;
        }
        return false;
    }

    /**
     * Fills in the blanks that aren't present on the client when playing on a
     * remote server.
     */
    protected void simulateMotionData(PlayerEntity ply) {
        clientPlayer = resolveToClientPlayer(ply);

        if (isClientPlayer(ply)) {
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
            return System.currentTimeMillis() - timeImmobile > VAR.IMMOBILE_DURATION;
        }

        return false;
    }

    protected void simulateFootsteps(PlayerEntity ply) {
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

            if (scalStat && VAR.PLAY_WANDER && !isolator.getSolver().hasStoppingConditions(ply)) {
                isolator.getSolver().playAssociation(ply, isolator.getSolver().findAssociation(ply, 0d, isRightFoot),
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
                distance = VAR.DISTANCE_LADDER;
            } else if (!ply.isInWater() && Math.abs(yPosition - ply.y) > 0.4) {
                // This ensures this does not get recorded as landing, but as a step
                if (yPosition < ply.y) { // Going upstairs
                    distance = VAR.DISTANCE_STAIR;
                    event = speedDisambiguator(ply, State.UP, State.UP_RUN);
                } else if (!ply.isSneaking()) { // Going downstairs
                    distance = -1f;
                    verticalOffsetAsMinus = 0f;
                    event = speedDisambiguator(ply, State.DOWN, State.DOWN_RUN);
                }

                dwmYChange = distanceReference;

            } else {
                distance = VAR.DISTANCE_HUMAN;
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

    protected void produceStep(PlayerEntity ply, State event) {
        produceStep(ply, event, 0d);
    }

    protected void produceStep(PlayerEntity ply, State event, double verticalOffsetAsMinus) {
        if (!isolator.getSolver().playStoppingConditions(ply)) {
            if (event == null)
                event = speedDisambiguator(ply, State.WALK, State.RUN);
            isolator.getSolver().playAssociation(ply,
                    isolator.getSolver().findAssociation(ply, verticalOffsetAsMinus, isRightFoot), event);
            isRightFoot = !isRightFoot;
        }

        stepThisFrame = true;
    }

    protected void stepped(PlayerEntity ply, State event) {

    }

    protected float reevaluateDistance(State event, float distance) {
        return distance;
    }

    protected void simulateAirborne(PlayerEntity ply) {
        if ((ply.onGround || ply.isClimbing()) == isAirborne) {
            isAirborne = !isAirborne;
            simulateJumpingLanding(ply);
        }
        if (isAirborne) {
            fallDistance = ply.fallDistance;
        }
    }

    protected boolean isJumping(PlayerEntity ply) {
        return ((ILivingEntity) ply).isJumping();
    }

    protected double getOffsetMinus(PlayerEntity ply) {
        if (ply instanceof OtherClientPlayerEntity) {
            return 1;
        }
        return 0;
    }

    protected void simulateJumpingLanding(PlayerEntity ply) {
        if (isolator.getSolver().hasStoppingConditions(ply))
            return;

        boolean isJumping = isJumping(ply);
        if (isAirborne && isJumping) {
            simulateJumping(ply);
        } else if (!isAirborne) {
            simulateLanding(ply);
        }
    }

    protected void simulateJumping(PlayerEntity ply) {
        if (VAR.EVENT_ON_JUMP) {
            double speed = motionX * motionX + motionZ * motionZ;
            if (speed < VAR.SPEED_TO_JUMP_AS_MULTIFOOT) {
                // STILL JUMP
                playMultifoot(ply, getOffsetMinus(ply) + 0.4d, State.JUMP);
                // 2 - 0.7531999805212d (magic number for vertical offset?)
            } else {
                playSinglefoot(ply, getOffsetMinus(ply) + 0.4d, State.JUMP, isRightFoot);
                // RUNNING JUMP
                // Do not toggle foot: After landing sounds, the first foot will be same as the
                // one used to jump.
            }
        }
    }

    protected void simulateLanding(PlayerEntity ply) {
        if (fallDistance > VAR.LAND_HARD_DISTANCE_MIN) {
            playMultifoot(ply, getOffsetMinus(ply), State.LAND); // Always assume the player lands on their two feet
            // Do not toggle foot: After landing sounds, the first foot will be same as the
            // one used to jump.
        } else if (/* !this.stepThisFrame && */!ply.isSneaking()) {
            playSinglefoot(ply, getOffsetMinus(ply), speedDisambiguator(ply, State.CLIMB, State.CLIMB_RUN),
                    isRightFoot);
            if (!this.stepThisFrame)
                isRightFoot = !isRightFoot;
        }
    }

    private boolean isClientPlayer(PlayerEntity ply) {
        return resolveToClientPlayer(ply) == ply;
    }

    private PlayerEntity resolveToClientPlayer(PlayerEntity ply) {
        PlayerEntity client = MinecraftClient.getInstance().player;

        if (client != null && (client == clientPlayer || client.getUuid().equals(clientPlayer.getUuid()))) {
            return client;
        }
        return ply;
    }

    protected State speedDisambiguator(PlayerEntity ply, State walk, State run) {
        if (!isClientPlayer(ply)) { // Other players don't send motion data, so have to decide some other way
            if (ply.isSprinting()) {
                return run;
            }
            return walk;
        }

        double velocity = motionX * motionX + motionZ * motionZ;
        return velocity > VAR.SPEED_TO_RUN ? run : walk;
    }

    private void simulateBrushes(PlayerEntity ply) {
        if (brushesTime > System.currentTimeMillis()) {
            return;
        }

        brushesTime = System.currentTimeMillis() + 100;

        if ((motionX == 0d && motionZ == 0d) || ply.isSneaking())
            return;

        int yy = MathHelper.floor(ply.y - 0.1d - ply.getHeightOffset() - (ply.onGround ? 0d : 0.25d));

        Association assos = isolator.getSolver().findAssociation(ply.world,
                MathHelper.floor(ply.x),
                yy,
                MathHelper.floor(ply.z),
                "find_messy_foliage");

        if (assos != null) {
            if (!isMessyFoliage) {
                isMessyFoliage = true;
                isolator.getSolver().playAssociation(ply, assos, State.WALK);
            }
        } else if (isMessyFoliage) {
            isMessyFoliage = false;
        }
    }

    protected void playSinglefoot(PlayerEntity ply, double verticalOffsetAsMinus, State eventType, boolean foot) {
        Association assos = isolator.getSolver().findAssociation(ply, verticalOffsetAsMinus, isRightFoot);
        if (assos == null || assos.isNotEmitter()) {
            assos = isolator.getSolver().findAssociation(ply, verticalOffsetAsMinus + 1, isRightFoot);
        }
        isolator.getSolver().playAssociation(ply, assos, eventType);
    }

    protected void playMultifoot(PlayerEntity ply, double verticalOffsetAsMinus, State eventType) {
        // STILL JUMP
        Association leftFoot = isolator.getSolver().findAssociation(ply, verticalOffsetAsMinus, false);
        Association rightFoot = isolator.getSolver().findAssociation(ply, verticalOffsetAsMinus, true);

        if (leftFoot != null && leftFoot.equals(rightFoot) && leftFoot.hasAssociation()) {
            rightFoot = null;
            // If the two feet solve to the same sound, except NO_ASSOCIATION, only play the sound once
        }

        isolator.getSolver().playAssociation(ply, leftFoot, eventType);
        isolator.getSolver().playAssociation(ply, rightFoot, eventType);
    }

    protected float scalex(float number, float min, float max) {
        float m = (number - min) / (max - min);
        return m < 0 ? 0 : m > 1 ? 1 : m;
    }
}
