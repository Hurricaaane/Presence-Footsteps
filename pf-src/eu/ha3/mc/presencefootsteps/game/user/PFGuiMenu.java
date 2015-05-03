package eu.ha3.mc.presencefootsteps.game.user;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenResourcePacks;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import eu.ha3.mc.gui.HDisplayStringProvider;
import eu.ha3.mc.gui.HGuiSliderControl;
import eu.ha3.mc.gui.HSliderListener;
import eu.ha3.mc.presencefootsteps.game.system.PFHaddon;

/* x-placeholder-wtfplv2 */

public class PFGuiMenu extends GuiScreen
{
	/**
	 * A reference to the screen object that created  Used for navigating
	 * between screens.
	 */
	private GuiScreen parentScreen;
	
	/** The title string that is displayed in the top-center of the screen. */
	protected String screenTitle;
	
	private PFHaddon mod;
	
	/** The ID of the button that has been pressed. */
	private int buttonId;
	
	private final int IDS_PER_PAGE = 4;
	private final int BUTTON_SPACING = 4;
	private final int BUTTON_HEIGHT = 20;
	private final int BUTTON_WIDTH = 300;
	
	
	public PFGuiMenu(GuiScreen par1GuiScreen, PFHaddon mod) {
		screenTitle = "Presence Footsteps Configuration";
		buttonId = -1;
		parentScreen = par1GuiScreen;
		this.mod = mod;
	}
	
	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void initGui() {
		final int TOP = 45;
		final int _MIX = BUTTON_SPACING + BUTTON_HEIGHT;
		final int _LEFT = width / 2 - BUTTON_WIDTH / 2;
		final int _RIGHT = width / 2 + BUTTON_WIDTH / 2;
		
		int id = 0;
		
		{
			HGuiSliderControl sliderControl = new HGuiSliderControl(id, _LEFT + _MIX * 2, TOP + _MIX, BUTTON_WIDTH - _MIX * 4 - BUTTON_HEIGHT - 5, BUTTON_HEIGHT, "", mod.getConfig().getInteger("user.volume.0-to-100") / 100f);
			sliderControl.setListener(new HSliderListener() {
				@Override
				public void sliderValueChanged(HGuiSliderControl slider, float value)
				{
					float valueSnapped = value * 100;
					if (valueSnapped % 10 <= 2)
					{
						valueSnapped = valueSnapped - valueSnapped % 10;
					}
					else if (valueSnapped % 10 >= 10 - 2)
					{
						valueSnapped = valueSnapped - valueSnapped % 10 + 10;
					}
					int valueSnappedInt = Math.round(valueSnapped);
					
					mod.getConfig().setProperty("user.volume.0-to-100", valueSnappedInt);
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
					return "Global Volume: " + mod.getConfig().getInteger("user.volume.0-to-100") + "%";
				}
			});
			sliderControl.updateDisplayString();
			
			buttonList.add(sliderControl);
			id++;
			
		}
		
		//final int _ASPLIT = 2;
		//final int _AWID = _WIDTH / _ASPLIT - _GAP * (_ASPLIT - 1) / 2;
		
		buttonList.add(new GuiButton(212, _LEFT + _MIX * 2 + (BUTTON_WIDTH - _MIX * 4 - BUTTON_HEIGHT), TOP + _MIX, BUTTON_HEIGHT, BUTTON_HEIGHT, mod.getEnabled() ? "On" : "Off"));
		
		buttonList.add(new GuiButton(210, _LEFT + _MIX * 2, TOP + _MIX * 2, BUTTON_WIDTH - _MIX * 4, BUTTON_HEIGHT,  "Walking stance: " + getStance()));
		
		buttonList.add(new GuiButton(199, _LEFT + _MIX * 2, TOP + _MIX * 3 + (IDS_PER_PAGE * 2), BUTTON_WIDTH - _MIX * 4, BUTTON_HEIGHT, getResourcePacks() + " Resource Pack"));
		
		buttonList.add(new GuiButton(198, _LEFT + _MIX * 2, TOP + _MIX * 4 + (IDS_PER_PAGE * 2), BUTTON_WIDTH - _MIX * 4, BUTTON_HEIGHT, "Generate custom block report"));
		
		buttonList.add(new GuiButton(220, _LEFT + _MIX * 2, TOP + _MIX * 5 + (IDS_PER_PAGE * 2), BUTTON_WIDTH - _MIX * 4, BUTTON_HEIGHT, "Generate unknown blocks report"));
		
		buttonList.add(new GuiButton(200, _LEFT + _MIX * 2, TOP + _MIX * 6 + (IDS_PER_PAGE * 4), BUTTON_WIDTH - _MIX * 4, BUTTON_HEIGHT, I18n.format("menu.returnToGame")));
	}
	
	private String getStance() {
		int stance = mod.getConfig().getInteger("custom.stance");
		return stance == 0 ? "AUTO" : stance == 1 ? "Quadrupedal" : "Bipedal";
	}
	
	private String getResourcePacks() {
		if (mod.hasResourcePacksLoaded()) {
			return "Configure";
		}
		return mod.hasNonethelessResourcePacksInstalled() ? "Enable" : "Install";
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
				mod.getChatter().printChat(EnumChatFormatting.RED, "Failed to generate custom block report: " + e.getMessage());
			}
		} else if (par1GuiButton.id == 210) {
			mod.getConfig().setProperty("custom.stance", (mod.getConfig().getInteger("custom.stance") + 1) % 3);
			mod.saveConfig();
			par1GuiButton.displayString = "Walking stance: " + getStance();
			
			mod.reloadEverything(false);
		} else if (par1GuiButton.id == 220) {
			try {
				(new BlockReport(mod)).generateUnknownReport().printResults("presencefootsteps/report_concise", ".txt");
			} catch (Exception e) {
				mod.getChatter().printChat(EnumChatFormatting.RED, "Failed to generate custom block report: " + e.getMessage());
			}
		} else if (par1GuiButton.id == 212) {
			par1GuiButton.displayString = mod.toggle() ? "On" : "Off";
		}
		
	}
	
	@Override
	public void onGuiClosed() {
		mod.saveConfig();
	}
	
	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked(int par1, int par2, int par3) throws IOException {
		if (buttonId < 0) {
			super.mouseClicked(par1, par2, par3);
		}
	}
	
	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		drawDefaultBackground();
		//drawGradientRect(0, 0, width, height, 0xC0000000, 0x60000000);
		
		drawCenteredString(fontRendererObj, screenTitle, width / 2, 40, 0xffffff);
		
		if (!mod.hasResourcePacksLoaded()) {
			if (mod.hasNonethelessResourcePacksInstalled()) {
				drawCenteredString(fontRendererObj, "Your Presence Footsteps Resource Pack isn't enabled yet!", width / 2, 10, 0xff0000);
				drawCenteredString(fontRendererObj, "Activate it in the Minecraft Options menu for it to run.", width / 2, 20, 0xff0000);
			} else {
				drawCenteredString(fontRendererObj, "You don't have any Presence Footsteps Resource Pack installed!", width / 2, 10, 0xff0000);
				drawCenteredString(fontRendererObj, "Put the Resource Pack in the resourcepacks/ folder.", width / 2, 20, 0xff0000);
			}
		}
		
		super.drawScreen(par1, par2, par3);
		
	}
	
}
