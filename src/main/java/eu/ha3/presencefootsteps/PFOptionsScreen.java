package eu.ha3.presencefootsteps;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.element.Slider;

import eu.ha3.presencefootsteps.resources.ResourcesState;
import eu.ha3.presencefootsteps.util.BlockReport;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.resourcepack.ResourcePackOptionsScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;

class PFOptionsScreen extends GameGui {

    private static final int yButtonSpacing = 24;
    private static final int yGroupSpacing = 56;
    private static final int xButtonSpacing = 95;

    private PresenceFootsteps mod;

    public PFOptionsScreen(PresenceFootsteps haddon) {
        super(new TranslatableText("menu.pf.title"), MinecraftClient.getInstance().currentScreen);
        mod = haddon;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void init() {
        final int center = width / 2;

        final int right = center + 102;

        int leftCol =  center - 102 - xButtonSpacing;
        int rightCol = center - 102 + xButtonSpacing;

        int row = 69;

        addButton(new Button(right + 32, row).onClick(sender ->
            sender.getStyle().setText("menu.pf." + mod.toggle()))
        ).getStyle()
            .setText("menu.pf." + mod.getConfig().getEnabled());

        addButton(new Slider(rightCol, row, 0, 100, mod.getConfig().getVolume() * 100))
            .onChange(volume -> {
                if (volume % 10 <= 2) {
                    volume -= volume % 10;
                } else if (volume % 10 >= 8) {
                    volume -= volume % 10 + 10;
                }

                mod.getConfig().setVolume(Math.round(volume / 100));

                return volume;
        }).setFormatter(volume -> I18n.translate("menu.pf.volume", formatVolume(volume)));

        addButton(new Button(leftCol, row).onClick(sender -> {
            mod.getConfig().setLocomotion(null);
            mod.getConfig().save();

            sender.setMessage(getStance());

            mod.getEngine().reloadEverything();
        })).getStyle()
            .setText(getStance());

        row += yGroupSpacing;

        addButton(new Button(rightCol, row).onClick(sender ->
            minecraft.openScreen(new ResourcePackOptionsScreen(this))
        )).getStyle()
            .setText(getResourcePacks());

        addButton(new Button(leftCol, row).onClick(sender -> {
            sender.getStyle().setText(mod.getConfig().toggleMultiplayer() ? "menu.pf.multiplayer.on" : "menu.pf.multiplayer.off");
        })).getStyle()
            .setText(getMP());

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

        addButton(new Button(center - 100, row).onClick(sender -> finish()))
            .getStyle()
            .setText("menu.returnToGame");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();

        drawCenteredString(font, getTitle().asString(), width / 2, 40, 0xFFFFFFFF);

        ResourcesState state = mod.getEngine().getResourcesState();

        if (!state.isFunctional()) {
            String text = "menu.pf.warn." + state.name().toLowerCase();
            drawCenteredString(font, I18n.translate(text + ".0"), width / 2, 10, 0xFFFF0000);
            drawCenteredString(font, I18n.translate(text + ".1"), width / 2, 20, 0xFFFF0000);
        }

        super.render(mouseX, mouseY, partialTicks);
    }

    private String formatVolume(float volume) {

        if (volume >= 100) {
            return I18n.translate("menu.pf.volume.max");
        }

        if (volume <= 0) {
            return I18n.translate("menu.pf.volume.min");
        }

        return volume + "%";
    }

    private String getMP() {
        return "menu.pf.multiplayer." + mod.getConfig().getEnabledMP();
    }

    private String getStance() {
        return I18n.translate("menu.pf.stance", I18n.translate(mod.getConfig().getLocomotion().getTranslationKey()));
    }

    private String getResourcePacks() {
        return "menu.pf.resourcepacks." + mod.getEngine().getResourcesState().name().toLowerCase();
    }

    @Override
    public void removed() {
        mod.getConfig().save();
    }
}
