package eu.ha3.presencefootsteps;

import com.google.common.base.Strings;

import eu.ha3.mc.quick.update.UpdateNotifier;
import eu.ha3.mc.quick.update.UpdateNotifier.Version;
import eu.ha3.presencefootsteps.resources.ResourcesState;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import eu.ha3.presencefootsteps.util.ChatUtil;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.SystemUtil;

class Checker {

    private final UpdateNotifier updateNotifier = new UpdateNotifier(
            "https://raw.githubusercontent.com/Sollace/Presence-Footsteps/master/version/versions.json?ver=%d")
            .setVersion("1.14.4", 13, "r")
            .setReporter(this::reportUpdate);

    private boolean requestedResourceFix;

    private boolean mlpDetectedFirst;

    private boolean performedCheck;

    private final PresenceFootsteps pf;

    Checker(PresenceFootsteps pf) {
        this.pf = pf;
    }

    void tryCheck() {
        if (!performedCheck) {
            performedCheck = true;
            updateNotifier.attempt();

            performCheck();
        }

        ResourcesState state = pf.getEngine().getResourcesState();

        if (!requestedResourceFix && !state.isFunctional()) {
            requestedResourceFix = true;

            String text = "pf.pack." + state.name().toLowerCase();

            ChatUtil.addMessage(SystemUtil.consume(new TranslatableText(text + ".0"),
                    l -> l.getStyle().setColor(Formatting.RED))
                    .append(new TranslatableText(text + ".1")));
        }

        if (requestedResourceFix && state.isFunctional()) {
            requestedResourceFix = false;

            ChatUtil.addMessage(SystemUtil.consume(new TranslatableText("pf.pack.yay"),
                    l -> l.getStyle().setColor(Formatting.GREEN)));
        }
    }

    private void reportUpdate(Version newVersion, Version currentVersion) {
        if (Strings.isNullOrEmpty(newVersion.minecraft)) {
            ChatUtil.addMessage(
                    SystemUtil.consume(new LiteralText("An update is available: " + newVersion.type + newVersion.number),
                            l -> l.getStyle().setColor(Formatting.GOLD)));
        } else if (currentVersion.minecraft.equals(newVersion.minecraft)) {
            ChatUtil.addMessage(
                    SystemUtil.consume(new LiteralText("An update is available for your version of Minecraft: " + newVersion.type + newVersion.number),
                            l -> l.getStyle().setColor(Formatting.GOLD)));
        } else {
            ChatUtil.addMessage(
                    SystemUtil.consume(new LiteralText("An update is available for "),
                            l -> l.getStyle().setColor(Formatting.GOLD))
                    .append(SystemUtil.consume(new LiteralText("another"),
                            l -> l.getStyle().setItalic(true)))
                    .append(SystemUtil.consume(new LiteralText(" version of Minecraft: " + newVersion.type + newVersion.number + " for " + newVersion.minecraft),
                            l -> l.getStyle().setColor(Formatting.GOLD)))
            );
        }

        int difference = newVersion.number - currentVersion.number;

        ChatUtil.addMessage(
                SystemUtil.consume(new LiteralText("You're "),
                        l -> l.getStyle().setColor(Formatting.GOLD))
                .append(SystemUtil.consume(new LiteralText(String.valueOf(difference)),
                        l -> l.getStyle().setColor(Formatting.WHITE)))
                .append(SystemUtil.consume(new LiteralText(" version" + (difference > 1 ? "s" : "") + " late."),
                        l -> l.getStyle().setColor(Formatting.GOLD)))
        );

        int remaining = updateNotifier.getRemainingNotifications();

        if (remaining > 0) {
            ChatUtil.addMessage(
                    SystemUtil.consume(new LiteralText("This message will display "),
                            l -> l.getStyle().setColor(Formatting.GOLD))
                    .append(SystemUtil.consume(new LiteralText(String.valueOf(remaining)),
                            l -> l.getStyle().setColor(Formatting.WHITE)))
                    .append(SystemUtil.consume(new LiteralText(" more time" + (remaining > 1 ? "s" : "") + " late."),
                            l -> l.getStyle().setColor(Formatting.GOLD)))
            );
        } else {
            ChatUtil.addMessage(
                    SystemUtil.consume(new LiteralText("You won't be notified any more unless a newer version comes out."),
                            l -> l.getStyle().setColor(Formatting.GRAY)));
        }
    }

    private void performCheck() {
        if (mlpDetectedFirst) {
            String msg = "pf.mlp.2";

            if (pf.getConfig().getLocomotion() == Locomotion.UNKNOWN) {
                msg += ".stance";
            }

            if (!pf.getKeyBinding().isNotBound()) {
                msg += ".button";
            }

            ChatUtil.addMessage(new TranslatableText("pf.mlp.0")
                    .append(SystemUtil.consume(new TranslatableText("pf.mlp.1"),
                            l -> l.getStyle().setColor(Formatting.AQUA)))
                    .append(SystemUtil.consume(
                            new TranslatableText(msg, pf.getKeyBinding().getLocalizedName()),
                            l -> l.getStyle().setColor(Formatting.GRAY))));
        }


    }
}
