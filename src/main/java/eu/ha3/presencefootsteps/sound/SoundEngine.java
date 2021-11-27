package eu.ha3.presencefootsteps.sound;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import eu.ha3.presencefootsteps.PFConfig;
import eu.ha3.presencefootsteps.mixins.IEntity;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticsJsonParser;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import eu.ha3.presencefootsteps.sound.generator.StepSoundGenerator;
import eu.ha3.presencefootsteps.util.ResourceUtils;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

public class SoundEngine implements IdentifiableResourceReloadListener {
    private static final Identifier BLOCK_MAP = new Identifier("presencefootsteps", "config/blockmap.json");
    private static final Identifier GOLEM_MAP = new Identifier("presencefootsteps", "config/golemmap.json");
    private static final Identifier LOCOMOTION_MAP = new Identifier("presencefootsteps", "config/locomotionmap.json");
    private static final Identifier PRIMITIVE_MAP = new Identifier("presencefootsteps", "config/primitivemap.json");
    private static final Identifier ACOUSTICS = new Identifier("presencefootsteps", "config/acoustics.json");
    private static final Identifier VARIATOR = new Identifier("presencefootsteps", "config/variator.json");

    private static final Identifier ID = new Identifier("presencefootsteps", "sounds");

    private PFIsolator isolator = new PFIsolator(this);

    private final PFConfig config;

    public SoundEngine(PFConfig config) {
        this.config = config;
    }

    public float getGlobalVolume() {
        return config.getVolume() / 100F;
    }

    public Isolator getIsolator() {
        return isolator;
    }

    public void reload() {
        if (config.getEnabled()) {
            reloadEverything(MinecraftClient.getInstance().getResourceManager());
        } else {
            shutdown();
        }
    }

    public boolean isRunning(MinecraftClient client) {
        return config.getEnabled() && (client.isInSingleplayer() || config.getEnabledMP());
    }

    private Stream<? extends Entity> getTargets(Entity cameraEntity) {
        return cameraEntity.world.getOtherEntities(null, cameraEntity.getBoundingBox().expand(16), e -> {
            return e instanceof LivingEntity
                    && !(e instanceof WaterCreatureEntity)
                    && !(e instanceof FlyingEntity)
                    && !e.hasVehicle()
                    && !((LivingEntity)e).isSleeping()
                    && e.distanceTo(cameraEntity) <= 16
                    && (config.getEnabledGlobal() || (e instanceof PlayerEntity));
        }).stream();
    }

    public void onFrame(MinecraftClient client, Entity cameraEntity) {
        if (!client.isPaused() && isRunning(client)) {
            getTargets(cameraEntity).forEach(e -> {
                StepSoundGenerator generator = ((StepSoundSource) e).getStepGenerator(this);
                generator.setIsolator(isolator);
                if (generator.generateFootsteps((LivingEntity)e)) {
                    ((IEntity) e).setNextStepDistance(Integer.MAX_VALUE);
                }
            });

            isolator.think(); // Delayed sounds
        }
    }

    public boolean onSoundRecieved(@Nullable SoundEvent event, SoundCategory category) {

        if (event == null || category != SoundCategory.PLAYERS || !isRunning(MinecraftClient.getInstance())) {
            return false;
        }

        if (event == SoundEvents.ENTITY_PLAYER_SWIM
         || event == SoundEvents.ENTITY_PLAYER_SPLASH
         || event == SoundEvents.ENTITY_PLAYER_BIG_FALL
         || event == SoundEvents.ENTITY_PLAYER_SMALL_FALL) {
            return true;
        }

        String[] name = event.getId().getPath().split("\\.");

        return name.length > 0
                && "block".contentEquals(name[0])
                && "step".contentEquals(name[name.length - 1]);
    }

    public Locomotion getLocomotion(LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            return Locomotion.forPlayer((PlayerEntity)entity, config.getLocomotion());
        }
        return isolator.getLocomotionMap().lookup(entity);
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer sync, ResourceManager sender,
            Profiler serverProfiler, Profiler clientProfiler,
            Executor serverExecutor, Executor clientExecutor) {
        return sync.whenPrepared(null).thenRunAsync(() -> {
            clientProfiler.startTick();
            clientProfiler.push("Reloading PF Sounds");
            reloadEverything(sender);
            clientProfiler.pop();
            clientProfiler.endTick();
        }, clientExecutor);
    }

    public void reloadEverything(ResourceManager manager) {
        isolator = new PFIsolator(this);

        ResourceUtils.forEach(BLOCK_MAP, manager, isolator.getBlockMap()::load);
        ResourceUtils.forEach(GOLEM_MAP, manager, isolator.getGolemMap()::load);
        ResourceUtils.forEach(PRIMITIVE_MAP, manager, isolator.getPrimitiveMap()::load);
        ResourceUtils.forEach(LOCOMOTION_MAP, manager, isolator.getLocomotionMap()::load);
        ResourceUtils.forEach(ACOUSTICS, manager, new AcousticsJsonParser(isolator.getAcoustics())::parse);
        ResourceUtils.forEach(VARIATOR, manager, isolator.getVariator()::load);
    }

    public void shutdown() {
        isolator = new PFIsolator(this);

        PlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null) {
            ((IEntity) player).setNextStepDistance(0);
        }
    }
}
