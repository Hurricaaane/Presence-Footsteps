package eu.ha3.mc.presencefootsteps.game.user;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenResourcePacks;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import eu.ha3.mc.gui.HDisplayStringProvider;
import eu.ha3.mc.gui.HGuiSliderControl;
import eu.ha3.mc.gui.HSliderListener;
import eu.ha3.mc.presencefootsteps.game.system.PFHaddon;

public class PFGuiMenu extends GuiScreen {
	/**
	 * A reference to the screen object that created  Used for navigating between screens.
	 */
	private GuiScreen parentScreen;
	
	/**
	 * The title string that is displayed in the top-center of the screen.
	 */
	protected String screenTitle;
	
	private PFHaddon mod;
	
	private final int TOP = 45;
	private final int BUTTON_SPACING = 4;
	private final int BUTTON_HEIGHT = 20;
	
	private final int Y_SPACING = BUTTON_SPACING + BUTTON_HEIGHT;
	private final int BUTTON_WIDTH = 300 - Y_SPACING * 4;
	
	
	public PFGuiMenu(GuiScreen par1GuiScreen, PFHaddon haddon) {
		screenTitle = I18n.format("menu.pf.title");
		parentScreen = par1GuiScreen;
		mod = haddon;
	}
	
	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void initGui() {
		final int _LEFT = width / 2 - 300 / 2 + Y_SPACING * 2;
		final int _RIGHT = width / 2 + 300 / 2 - Y_SPACING * 2;
		
		HGuiSliderControl sliderControl = new HGuiSliderControl(0, _LEFT, TOP + Y_SPACING, BUTTON_WIDTH - BUTTON_HEIGHT - 5, BUTTON_HEIGHT, "", mod.getVolume() / 100f);
		sliderControl.setListener(new HSliderListener() {
			@Override
			public void sliderValueChanged(HGuiSliderControl slider, float value) {
				float valueSnapped = value * 100;
				
				if (valueSnapped > 100) valueSnapped = 100;
				if (valueSnapped < 0) valueSnapped = 0;
				
				if (valueSnapped % 10 <= 2) {
					valueSnapped = valueSnapped - valueSnapped % 10;
				} else if (valueSnapped % 10 >= 8) {
					valueSnapped = valueSnapped - valueSnapped % 10 + 10;
				}
				
				mod.setVolume(Math.round(valueSnapped));
				slider.updateDisplayString();
			}
			
			@Override
			public void sliderPressed(HGuiSliderControl hGuiSliderControl) {}
			
			@Override
			public void sliderReleased(HGuiSliderControl hGuiSliderControl) {}
		});
		sliderControl.setDisplayStringProvider(new HDisplayStringProvider() {
			@Override
			public String provideDisplayString() {
				int volume = mod.getVolume();
				return I18n.format("menu.pf.volume") + ": " + (volume >= 100 ? I18n.format("menu.pf.volume.max") : volume <= 0 ? I18n.format("menu.pf.volume.min") : (volume + "%"));
			}
		});
		sliderControl.updateDisplayString();
		
		buttonList.add(sliderControl);
		buttonList.add(new GuiButton(212, _RIGHT - BUTTON_HEIGHT, TOP + Y_SPACING, BUTTON_HEIGHT, BUTTON_HEIGHT, I18n.format(mod.getEnabled() ? "menu.pf.on" : "menu.pf.off")));
		buttonList.add(new GuiButton(210, _LEFT, TOP + Y_SPACING * 2, BUTTON_WIDTH, BUTTON_HEIGHT, getStance()));
		buttonList.add(new GuiButton(199, _LEFT, TOP + Y_SPACING * 3 + (BUTTON_SPACING * 2), BUTTON_WIDTH, BUTTON_HEIGHT, I18n.format(getResourcePacks())));
		buttonList.add(new GuiButton(198, _LEFT, TOP + Y_SPACING * 4 + (BUTTON_SPACING * 2), BUTTON_WIDTH, BUTTON_HEIGHT, I18n.format("menu.pf.report.full")));
		buttonList.add(new GuiButton(220, _LEFT, TOP + Y_SPACING * 5 + (BUTTON_SPACING * 2), BUTTON_WIDTH, BUTTON_HEIGHT, I18n.format("menu.pf.report.concise")));
		buttonList.add(new GuiButton(200, _LEFT, TOP + Y_SPACING * 6 + (BUTTON_SPACING * 4), BUTTON_WIDTH, BUTTON_HEIGHT, I18n.format("menu.returnToGame")));
	}
	
	private String getStance() {
		return I18n.format("menu.pf.stance") + ": " + I18n.format("menu.pf.stance." + mod.getConfig().getInteger("custom.stance"));
	}
	
	private String getResourcePacks() {
		return "menu.pf.resourcepacks." + (mod.hasResourcePacksLoaded() ? "loaded" : mod.hasResourcePacksInstalled() ? "disabled" : "none");
	}
	
	/**
	 * Fired when a control is clicked. This is the equivalent of
	 * ActionListener.actionPerformed(ActionEvent e).
	 */
	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		if (par1GuiButton.id == 200) {
			mc.displayGuiScreen(parentScreen); // This triggers onGuiClosed
		} else if (par1GuiButton.id == 199) {
			mc.displayGuiScreen(new GuiScreenResourcePacks(this)); // This triggers onGuiClosed
		} else if (par1GuiButton.id == 198) {
			try {
				(new BlockReport(mod)).generateReport().printResults("presencefootsteps/report_full", ".txt");
			} catch (Exception e) {
				mod.getChatter().printChat(EnumChatFormatting.RED, I18n.format("pf.report.error", e.getMessage()));
			}
		} else if (par1GuiButton.id == 210) {
			mod.getConfig().setProperty("custom.stance", (mod.getConfig().getInteger("custom.stance") + 1) % 3);
			mod.saveConfig();
			par1GuiButton.displayString = getStance();
			mod.reloadEverything(false);
		} else if (par1GuiButton.id == 220) {
			try {
				(new BlockReport(mod)).generateUnknownReport().printResults("presencefootsteps/report_concise", ".txt");
			} catch (Exception e) {
				mod.getChatter().printChat(EnumChatFormatting.RED, I18n.format("pf.report.error", e.getMessage()));
			}
		} else if (par1GuiButton.id == 212) {
			par1GuiButton.displayString = I18n.format(mod.toggle() ? "menu.pf.on" : "menu.pf.off");
		}
		
	}
	
	@Override
	public void onGuiClosed() {
		mod.saveConfig();
	}
	
	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		drawDefaultBackground();
		drawCenteredString(fontRendererObj, screenTitle, width / 2, 40, 0xffffff);
		if (!mod.hasResourcePacksLoaded()) {
			if (mod.hasResourcePacksInstalled()) {
				drawCenteredString(fontRendererObj, I18n.format("menu.pf.warn.0"), width / 2, 10, 0xff0000);
				drawCenteredString(fontRendererObj, I18n.format("menu.pf.warn.1"), width / 2, 20, 0xff0000);
			} else {
				drawCenteredString(fontRendererObj, I18n.format("menu.pf.warn.2"), width / 2, 10, 0xff0000);
				drawCenteredString(fontRendererObj, I18n.format("menu.pf.warn.3"), width / 2, 20, 0xff0000);
			}
		}
		super.drawScreen(par1, par2, par3);
	}
	
}
