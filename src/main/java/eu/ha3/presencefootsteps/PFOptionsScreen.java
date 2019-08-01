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

    private static final int yButtonSpacing = 24;
    private static final int yGroupSpacing = 56;
    private static final int xButtonSpacing = 105;

    private final PresenceFootsteps mod = PresenceFootsteps.getInstance();

    public PFOptionsScreen(@Nullable Screen parent) {
        super(new TranslatableText("menu.pf.title"), parent);
    }

    @Override
    public void init() {
        final int center = width / 2;

        int leftCol =  center - 102 - xButtonSpacing;
        int rightCol = center - 102 + xButtonSpacing;

        int row = 69;

        addButton(new Label(width / 2, 40)).setCentered().getStyle()
                .setText(getTitle().asString());

        addButton(new Slider(rightCol, row, 0, 100, mod.getConfig().getVolume()))
            .onChange(mod.getConfig()::setVolume)
            .setFormatter(this::formatVolume);

        addButton(new EnumSlider<>(leftCol, row, mod.getConfig().getLocomotion()).onChange(loco -> {
            mod.getConfig().setLocomotion(loco);
            mod.getEngine().reloadEverything(minecraft.getResourceManager());

            return loco;
        }).setFormatter(Locomotion::getDisplayName));

        row += yGroupSpacing;

        addButton(new Button(rightCol, row).onClick(sender ->
            sender.getStyle().setText("menu.pf." + mod.getEngine().toggle()))
        ).getStyle()
            .setText("menu.pf." + mod.getConfig().getEnabled());

        addButton(new Button(leftCol, row).onClick(sender -> {
            sender.getStyle().setText("menu.pf.multiplayer." + mod.getConfig().toggleMultiplayer());
        })).getStyle()
            .setText("menu.pf.multiplayer." + mod.getConfig().getEnabledMP());

        row += yButtonSpacing;

        addButton(new Button(rightCol, row).onClick(sender -> {
            new BlockReport("presencefootsteps/report_concise", ".txt").execute(state ->
                !mod.getEngine().getIsolator().getBlockMap().contains(state));
        })).getStyle()
            .setText("menu.pf.report.concise");

        addButton(new Button(leftCol, row).onClick(sender -> {
            new BlockReport("presencefootsteps/report_full", ".txt").execute(null);
        })).getStyle()
            .setText("menu.pf.report.full");

        row += yGroupSpacing;

        addButton(new Button(center - 100, row).onClick(sender -> finish())).getStyle()
            .setText("menu.returnToGame");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        super.render(mouseX, mouseY, partialTicks);
    }

    private String formatVolume(float volume) {

        if (volume <= 0) {
            return I18n.translate("menu.pf.volume.min");
        }

        return I18n.translate("menu.pf.volume", volume);
    }
}
