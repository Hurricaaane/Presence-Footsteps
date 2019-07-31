package eu.ha3.presencefootsteps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import eu.ha3.presencefootsteps.sound.SoundEngine;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class PresenceFootsteps implements ClientModInitializer {

    public static final Logger logger = LogManager.getLogger("PFSolver");

    public static PresenceFootsteps INSTANCE;

    private PFConfig config;

    private long pressedOptionsTime;

    private final Checker checker = new Checker(this);

    private final SoundEngine engine = new SoundEngine(config);

    private FabricKeyBinding keyBinding;

    public PresenceFootsteps() {
        INSTANCE = this;
    }

    public KeyBinding getKeyBinding() {
        return keyBinding;
    }

    public SoundEngine getEngine() {
        return engine;
    }

    public PFConfig getConfig() {
        return config;
    }

    @Override
    public void onInitializeClient() {
        keyBinding = FabricKeyBinding.Builder.create(new Identifier("presencefootsteps", "settings"),
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F9, "key.categories.misc").build();

        config = new PFConfig(FabricLoader.getInstance()
                .getConfigDirectory().toPath()
                .resolve("presencefootsteps")
                .resolve("userconfig.cfg"));

        KeyBindingRegistry.INSTANCE.register(keyBinding);
        ClientTickCallback.EVENT.register(this::onTick);
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(engine);
    }

    private void onTick(MinecraftClient client) {
        PlayerEntity ply = MinecraftClient.getInstance().player;
        if (ply == null) {
            return;
        }

        long handle = client.window.getHandle();

        boolean keysDown = InputUtil.isKeyPressed(handle, GLFW.GLFW_MOD_CONTROL)
                && InputUtil.isKeyPressed(handle, GLFW.GLFW_MOD_SHIFT)
                && InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_F);

        if (keysDown && System.currentTimeMillis() - pressedOptionsTime > 1000) {
            if (client.currentScreen == null) {
                client.openScreen(new PFOptionsScreen(this));

                if (client.isInSingleplayer()) {
                    client.getSoundManager().pauseAll();
                }
            }
        }

        engine.onTick(client);
        checker.tryCheck();
    }

    public boolean toggle() {
        if (config.toggleEnabled()) {
            engine.reloadEverything();
        } else {
            engine.shutdown();
        }

        return config.getEnabled();
    }
}
