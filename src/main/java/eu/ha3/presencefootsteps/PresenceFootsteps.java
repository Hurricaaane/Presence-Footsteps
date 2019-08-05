package eu.ha3.presencefootsteps;

import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import eu.ha3.mc.quick.update.UpdateNotifier;
import eu.ha3.mc.quick.update.UpdateNotifier.Version;
import eu.ha3.presencefootsteps.sound.SoundEngine;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class PresenceFootsteps implements ClientModInitializer {

    public static final Logger logger = LogManager.getLogger("PFSolver");

    private static PresenceFootsteps instance;

    public static PresenceFootsteps getInstance() {
        return instance;
    }

    private SoundEngine engine;

    private PFConfig config;

    private PFDebugHud debugHud;

    private UpdateNotifier updateNotifier;

    private FabricKeyBinding keyBinding;

    public PresenceFootsteps() {
        instance = this;
    }

    public PFDebugHud getDebugHud() {
        return debugHud;
    }

    public SoundEngine getEngine() {
        return engine;
    }

    public PFConfig getConfig() {
        return config;
    }

    @Override
    public void onInitializeClient() {

        Path pfFolder = FabricLoader.getInstance()
                .getConfigDirectory().toPath()
                .resolve("presencefootsteps");

        updateNotifier = new UpdateNotifier(
                pfFolder.resolve("updater.json"),
                "https://raw.githubusercontent.com/Sollace/Presence-Footsteps/master/version/versions.json?ver=%d",
                new UpdateNotifier.Version("1.14.4", "r", 15), this::onUpdate);
        updateNotifier.load();

        config = new PFConfig(pfFolder.resolve("userconfig.json"), this);
        config.load();

        keyBinding = FabricKeyBinding.Builder.create(new Identifier("presencefootsteps", "settings"),
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F10, "key.categories.misc").build();

        KeyBindingRegistry.INSTANCE.register(keyBinding);

        engine = new SoundEngine(config);
        debugHud = new PFDebugHud(engine);

        ClientTickCallback.EVENT.register(this::onTick);
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(engine);
    }

    private void onTick(MinecraftClient client) {
        PlayerEntity ply = client.player;

        if (ply == null || ply.removed) {
            return;
        }

        if (keyBinding.isPressed() && client.currentScreen == null) {
            client.openScreen(new PFOptionsScreen(client.currentScreen));
        }

        engine.onTick(client, ply);
        updateNotifier.attempt();
    }

    private void onUpdate(Version newVersion, Version currentVersion) {
        ToastManager manager = MinecraftClient.getInstance().getToastManager();

        SystemToast.show(manager, SystemToast.Type.TUTORIAL_HINT,
                new TranslatableText("pf.update.title"),
                new TranslatableText("pf.update.text", newVersion.type, newVersion.number, newVersion.minecraft));
    }
}
