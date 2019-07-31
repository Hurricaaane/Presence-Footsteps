package eu.ha3.presencefootsteps;

import eu.ha3.mc.quick.update.UpdateNotifier;
import eu.ha3.mc.quick.update.UpdateNotifier.Version;
import eu.ha3.presencefootsteps.sound.ResourcesState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.TranslatableText;

class Checker {

    private final UpdateNotifier updateNotifier = new UpdateNotifier(
            "https://raw.githubusercontent.com/Sollace/Presence-Footsteps/master/version/versions.json?ver=%d")
            .setVersion("1.14.4", 13, "r")
            .setReporter(this::reportUpdate);

    private boolean requestedResourceFix;

    private final PresenceFootsteps pf;

    Checker(PresenceFootsteps pf) {
        this.pf = pf;
    }

    void tryCheck() {
        updateNotifier.attempt();

        ResourcesState state = pf.getEngine().getResourcesState();
        ToastManager manager = MinecraftClient.getInstance().getToastManager();

        if (!requestedResourceFix && !state.isFunctional()) {
            requestedResourceFix = true;

            String text = "pf.pack." + state.name().toLowerCase();

            SystemToast.show(manager, SystemToast.Type.TUTORIAL_HINT, new TranslatableText(text + ".0"), new TranslatableText(text + ".1"));
        }

        if (requestedResourceFix && state.isFunctional()) {
            requestedResourceFix = false;

            SystemToast.show(manager, SystemToast.Type.TUTORIAL_HINT, new TranslatableText("menu.pf.title"), new TranslatableText("pf.pack.yay"));
        }
    }

    private void reportUpdate(Version newVersion, Version currentVersion) {
        ToastManager manager = MinecraftClient.getInstance().getToastManager();

        SystemToast.show(manager, SystemToast.Type.TUTORIAL_HINT,
                new TranslatableText("pf.update.title"),
                new TranslatableText("pf.update.text", newVersion.type, newVersion.number, newVersion.minecraft));
    }
}
