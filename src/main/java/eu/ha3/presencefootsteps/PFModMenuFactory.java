package eu.ha3.presencefootsteps;

import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

import io.github.prospector.modmenu.api.ModMenuApi;

public class PFModMenuFactory implements ModMenuApi {

    @Override
    public String getModId() {
        return "presencefootsteps";
    }

    @Override
    public Function<Screen, ? extends Screen> getConfigScreenFactory() {
        return PFOptionsScreen::new;
    }
}
