package eu.ha3.presencefootsteps.resources;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import com.google.gson.JsonParseException;

import eu.ha3.presencefootsteps.PFConfig;
import eu.ha3.presencefootsteps.PresenceFootsteps;
import eu.ha3.presencefootsteps.config.ConfigReader;
import eu.ha3.presencefootsteps.config.UserConfigSoundPlayerWrapper;
import eu.ha3.presencefootsteps.mixins.IEntity;
import eu.ha3.presencefootsteps.sound.Isolator;
import eu.ha3.presencefootsteps.sound.PFIsolator;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticsPlayer;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import eu.ha3.presencefootsteps.sound.generator.StepSoundGenerator;
import eu.ha3.presencefootsteps.world.PFSolver;
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

    private boolean hasResourcePacks;
    private boolean hasDisabledResourcePacks;

    public SoundEngine(PFConfig config) {
        this.config = config;
    }

    public Isolator getIsolator() {
        return isolator;
    }

    public ResourcesState getResourcesState() {
        if (hasResourcePacks) {
            return ResourcesState.LOADED;
        }

        if (hasDisabledResourcePacks) {
            return ResourcesState.UNLOADED;
        }

        return ResourcesState.NONE;
    }

    public void onTick(MinecraftClient client) {
        if (client.player == null) {
            return;
        }

        if (config.getEnabled() && hasResourcePacks && !client.isPaused()) {
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
    public CompletableFuture<Void> reload(Synchronizer var1, ResourceManager var2,
            Profiler var3, Profiler var4,
            Executor var5, Executor var6) {
        // TODO Auto-generated method stub

        PresenceFootsteps.logger.info("Resource Pack reload detected...");
        reloadEverything();

        return null;
    }

    public void reloadEverything() {
        isolator = new PFIsolator(this);

        config.load();

        List<ResourcePack> repo = dealer.findResourcePacks().collect(Collectors.toList());

        hasResourcePacks = !repo.isEmpty();
        hasDisabledResourcePacks = dealer.findDisabledResourcePacks().count() > 0;

        if (hasResourcePacks) {
            PresenceFootsteps.logger.info("Presence Footsteps didn't find any compatible resource packs.");
        } else {
            reloadBlocks(repo);
            reloadPrimitiveMap(repo);
            reloadAcoustics(repo);

            isolator.setSolver(new PFSolver(isolator));

            reloadVariator(repo);
        }

        isolator.reset();
    }

    public void shutdown() {
        isolator = new PFIsolator(this);

        config.load();
        isolator.reset();

        ((IEntity) MinecraftClient.getInstance().player).setNextStepDistance(0);
    }

    private void reloadVariator(List<ResourcePack> repo) {
        dealer.collectResources(ResourceDealer.variator, repo, isolator.getVariator()::load, ConfigReader::new);
    }

    private void reloadBlocks(List<ResourcePack> repo) {
        isolator.getBlockMap().clear();
        dealer.collectResources(ResourceDealer.blockmap, repo, isolator.getBlockMap()::load, ConfigReader::new);
    }

    private void reloadPrimitiveMap(List<ResourcePack> repo) {
        isolator.getPrimitiveMap().clear();
        dealer.collectResources(ResourceDealer.primitivemap, repo, isolator.getPrimitiveMap()::load, ConfigReader::new);
    }

    private void reloadAcoustics(List<ResourcePack> repo) {
        AcousticsPlayer acoustics = new AcousticsPlayer(isolator);

        dealer.collectResources(ResourceDealer.primitivemap, repo, scanner -> {
            try {
                new AcousticsJsonReader("").parseJson(scanner.useDelimiter("\\Z").next(), acoustics);
            } catch (JsonParseException e) {
                PresenceFootsteps.logger.error("Error whilst loading acoustics", e);
            }
        }, Scanner::new);

        isolator.setAcoustics(acoustics);
        isolator.setSoundPlayer(new UserConfigSoundPlayerWrapper(acoustics, config));
    }
}
