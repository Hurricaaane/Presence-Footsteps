package eu.ha3.presencefootsteps.sound;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import eu.ha3.presencefootsteps.PFConfig;
import eu.ha3.presencefootsteps.mixins.IEntity;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticsJsonParser;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import eu.ha3.presencefootsteps.sound.generator.StepSoundGenerator;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

public class SoundEngine implements IdentifiableResourceReloadListener {

    private static final Identifier ID = new Identifier("presencefootsteps", "sounds");

    private final ResourceDealer dealer = new ResourceDealer();

    private Isolator isolator;

    private final PFConfig config;

    public SoundEngine(PFConfig config) {
        this.config = config;
    }

    public Isolator getIsolator() {
        return isolator;
    }

    public void onTick(MinecraftClient client) {
        if (client.player == null) {
            return;
        }

        if (config.getEnabled() && !client.isPaused()) {
            if (!config.getEnabledMP()) {
                isolator.onFrame(client.player);
            }

            ((IEntity) client.player).setNextStepDistance(Integer.MAX_VALUE);
        }
    }

    public boolean onSoundRecieved(SoundEvent event, SoundCategory category) {

        if (!config.getEnabled() || category != SoundCategory.PLAYERS) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        boolean isMultiplayer = !(client.isInSingleplayer() || client.isIntegratedServerRunning());

        if (isMultiplayer && !config.getEnabledMP()) {
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

    public StepSoundGenerator supplyGenerator(PlayerEntity player) {
        return Locomotion.forPlayer(player, config.getLocomotion()).supplyGenerator(isolator);
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }


    @Override
    public CompletableFuture<Void> reload(Synchronizer sync, ResourceManager sender,
            Profiler serverProfiler, Profiler clientProfiler,
            Executor serverExecutor, Executor clientExecutor) {

        sync.getClass();
        return sync.whenPrepared(null).thenRunAsync(() -> {
            clientProfiler.startTick();
            clientProfiler.push("Reloading PF Sounds");
            reloadEverything();
            clientProfiler.pop();
            clientProfiler.endTick();
        }, clientExecutor);
    }

    public void reloadEverything() {
        List<ResourcePack> repo = dealer.findResourcePacks().collect(Collectors.toList());

        isolator = new PFIsolator(this, config);

        dealer.collectResources(ResourceDealer.blockmap, repo, isolator.getBlockMap()::load);
        dealer.collectResources(ResourceDealer.primitivemap, repo, isolator.getPrimitiveMap()::load);
        dealer.collectResources(ResourceDealer.primitivemap, repo, new AcousticsJsonParser(isolator.getAcoustics())::parse);
        dealer.collectResources(ResourceDealer.variator, repo, isolator.getVariator()::load);
    }

    public void shutdown() {
        isolator = new PFIsolator(this, config);

        ((IEntity) MinecraftClient.getInstance().player).setNextStepDistance(0);
    }
}
