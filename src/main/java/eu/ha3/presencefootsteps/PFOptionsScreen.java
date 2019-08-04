package eu.ha3.presencefootsteps;

import javax.annotation.Nullable;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.element.EnumSlider;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.common.client.gui.element.Slider;

import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import eu.ha3.presencefootsteps.util.BlockReport;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;

class PFOptionsScreen extends GameGui {

    public PFOptionsScreen(@Nullable Screen parent) {
        super(new TranslatableText("menu.pf.title"), parent);
    }

    @Override
    public void init() {
        int left =  width / 2 - 100;
        int row = height / 4 + 14;

        PFConfig config = PresenceFootsteps.getInstance().getConfig();

        addButton(new Label(width / 2, 30)).setCentered().getStyle()
                .setText(getTitle().asString());

        addButton(new Slider(left, row, 0, 100, config.getVolume()))
            .onChange(config::setVolume)
            .setFormatter(this::formatVolume);

        addButton(new EnumSlider<>(left, row += 24, config.getLocomotion())
                .onChange(config::setLocomotion)
                .setFormatter(Locomotion::getDisplayName));

        addButton(new Button(left, row += 24).onClick(sender -> {
            sender.getStyle().setText("menu.pf.multiplayer." + config.toggleMultiplayer());
        })).getStyle()
            .setText("menu.pf.multiplayer." + config.getEnabledMP());

        addButton(new Button(left, row += 24, 96, 20).onClick(sender -> {
            new BlockReport("report_concise").execute(state -> !PresenceFootsteps.getInstance().getEngine().getIsolator().getBlockMap().contains(state));
        })).getStyle()
            .setText("menu.pf.report.concise");

        addButton(new Button(left + 104, row, 96, 20)
            .onClick(sender -> new BlockReport("report_full").execute(null)))
            .getStyle()
                .setText("menu.pf.report.full");

        addButton(new Button(left, row += 34)
            .onClick(sender -> finish())).getStyle()
            .setText("gui.done");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        super.render(mouseX, mouseY, partialTicks);
    }

    private String formatVolume(float volume) {
        if (volume <= 0) {
            return "menu.pf.volume.min";
        }

        return I18n.translate("menu.pf.volume", (int)Math.floor(volume));
    }
}
