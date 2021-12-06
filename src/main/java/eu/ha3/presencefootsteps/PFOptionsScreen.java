package eu.ha3.presencefootsteps;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.element.AbstractSlider;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.element.EnumSlider;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.common.client.gui.element.Slider;

import eu.ha3.mc.quick.update.Versions;
import eu.ha3.presencefootsteps.util.BlockReport;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

class PFOptionsScreen extends GameGui {

    public PFOptionsScreen(@Nullable Screen parent) {
        super(new TranslatableText("menu.pf.title"), parent);
    }

    @Override
    public void init() {
        int left = width / 2 - 100;
        int row = height / 4 + 14;

        PFConfig config = PresenceFootsteps.getInstance().getConfig();

        addButton(new Label(width / 2, 30)).setCentered().getStyle()
                .setText(getTitle());

        redrawUpdateButton(addButton(new Button(width - 45, 20, 25, 20)).onClick(sender -> {
            sender.setEnabled(false);
            sender.getStyle().setTooltip("pf.update.checking");
            PresenceFootsteps.getInstance().getUpdateChecker().checkNow().thenAccept(newVersions -> {
                redrawUpdateButton(sender);
            });
        }));

        addButton(new Slider(left, row, 0, 100, config.getVolume()))
            .onChange(config::setVolume)
            .setTextFormat(this::formatVolume);

        addButton(new EnumSlider<>(left, row += 24, config.getLocomotion())
                .onChange(config::setLocomotion)
                .setTextFormat(v -> v.getValue().getOptionName()));

        addButton(new Button(left, row += 24).onClick(sender -> {
            sender.getStyle().setText("menu.pf.multiplayer." + config.toggleMultiplayer());
        })).getStyle()
            .setText("menu.pf.multiplayer." + config.getEnabledMP());

        addButton(new Button(left, row += 24).onClick(sender -> {
            sender.getStyle().setText("menu.pf.global." + config.toggleGlobal());
        })).getStyle()
            .setText("menu.pf.global." + config.getEnabledGlobal());

        addButton(new Button(left, row += 24, 96, 20).onClick(sender -> {
            sender.setEnabled(false);
            new BlockReport("report_concise")
                .execute(state -> !PresenceFootsteps.getInstance().getEngine().getIsolator().getBlockMap().contains(state))
                .thenRun(() -> sender.setEnabled(true));
        })).setEnabled(client.world != null)
            .getStyle()
            .setText("menu.pf.report.concise");

        addButton(new Button(left + 104, row, 96, 20)
            .onClick(sender -> {
                sender.setEnabled(false);
                new BlockReport("report_full")
                    .execute(null)
                    .thenRun(() -> sender.setEnabled(true));
            }))
            .setEnabled(client.world != null)
            .getStyle()
                .setText("menu.pf.report.full");

        addButton(new Button(left, row += 34)
            .onClick(sender -> finish())).getStyle()
            .setText("gui.done");
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, partialTicks);
    }

    private void redrawUpdateButton(Button button) {
        Optional<Versions> versions = PresenceFootsteps.getInstance().getUpdateChecker().getNewer();
        boolean hasUpdate = versions.isPresent();
        button.setEnabled(true);
        button.getStyle()
           .setText(hasUpdate ? "!" : ":)")
           .setColor(hasUpdate ? 0xAAFF00 : 0xFFFFFF)
           .setTooltip(versions
                   .map(Versions::latest)
                   .map(latest -> (Text)new TranslatableText("pf.update.updates_available",
                           latest.version().getFriendlyString(),
                           latest.minecraft().getFriendlyString()))
                   .orElse(new LiteralText("pf.update.up_to_date")));
    }

    private Text formatVolume(AbstractSlider<Float> slider) {
        if (slider.getValue() <= 0) {
            return new TranslatableText("menu.pf.volume.min");
        }

        return new TranslatableText("menu.pf.volume", (int)Math.floor(slider.getValue()));
    }
}
