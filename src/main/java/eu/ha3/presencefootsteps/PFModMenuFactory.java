package eu.ha3.presencefootsteps;

import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

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
