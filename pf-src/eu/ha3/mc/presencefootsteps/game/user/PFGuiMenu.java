package eu.ha3.mc.presencefootsteps.game.user;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenResourcePacks;
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
	
	private final int IDS_PER_PAGE = 5;
	
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
		final int _GAP = 2;
		final int _UNIT = 20;
		final int _WIDTH = 155 * 2;
		
		final int _MIX = _GAP + _UNIT;
		
		final int _LEFT = width / 2 - _WIDTH / 2;
		//final int _RIGHT = width / 2 + _WIDTH / 2;
		
		int id = 0;
		
		{
			HGuiSliderControl sliderControl = new HGuiSliderControl(id, _LEFT, _MIX, _WIDTH, _UNIT, "", mod.getConfig().getInteger("user.volume.0-to-100") / 100f);
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
					return "Global Volume Control: " + mod.getConfig().getInteger("user.volume.0-to-100") + "%";
				}
			});
			sliderControl.updateDisplayString();
			
			buttonList.add(sliderControl);
			id++;
			
		}
		
		//final int _ASPLIT = 2;
		//final int _AWID = _WIDTH / _ASPLIT - _GAP * (_ASPLIT - 1) / 2;
		
		final int _SEPARATOR = 9;
		
		buttonList.add(new GuiButton(210, _LEFT, _SEPARATOR + _MIX * 2, _WIDTH, _UNIT,  "Walking stance: " + getStance()));
		
		if (!mod.hasResourcePacksLoaded()) {
			buttonList.add(new GuiButton(199, _LEFT + _MIX, _SEPARATOR + _MIX * (IDS_PER_PAGE), _WIDTH - _MIX * 2, _UNIT, (mod.hasNonethelessResourcePacksInstalled() ? "Enable" : "Install") + " Presence Footsteps Resource Pack"));
		}
		
		buttonList.add(new GuiButton(198, _LEFT + _MIX * 2, _SEPARATOR + _MIX * (IDS_PER_PAGE + 1), _WIDTH - _MIX * 4, _UNIT, "Generate custom block report"));
		
		//final int _TURNOFFWIDTH = _WIDTH / 5;
		
		buttonList.add(new GuiButton(220, _LEFT + _MIX * 2, _SEPARATOR + _MIX * (IDS_PER_PAGE + 2), _WIDTH - _MIX * 4, _UNIT, "Generate unknown blocks report"));
		
		buttonList.add(new GuiButton(200, _LEFT + _MIX * 2, _SEPARATOR + _MIX * (IDS_PER_PAGE + 3), _WIDTH - _MIX * 4, _UNIT, "Done"));
		
		//buttonList.add(new GuiButton(212, _RIGHT - _TURNOFFWIDTH - _MIX, _SEPARATOR + _MIX * (IDS_PER_PAGE + 4), _TURNOFFWIDTH, _UNIT, "Turn Off"));
	}
	
	private String getStance() {
		int stance = mod.getConfig().getInteger("custom.stance");
		return stance == 0 ? "AUTO" : stance == 1 ? "Quadrupedal" : "Bipedal";
	}
	
	/**
	 * Fired when a control is clicked. This is the equivalent of
	 * ActionListener.actionPerformed(ActionEvent e).
	 */
	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		if (par1GuiButton.id == 200) {
			// This triggers onGuiClosed
			mc.displayGuiScreen(parentScreen);
		} else if (par1GuiButton.id == 199) {
			// This triggers onGuiClosed
			mc.displayGuiScreen(new GuiScreenResourcePacks(this));
		} else if (par1GuiButton.id == 198) {
			try {
				(new BlockReport(mod)).generateReport().printResults("presencefootsteps/report.txt");
			} catch (Exception e) {
				mod.getChatter().printChat(EnumChatFormatting.RED, "Failed to generate custom block report: " + e.getMessage());
			}
		}
		else if (par1GuiButton.id == 210)
		{
			int newEnabledState = mod.getConfig().getInteger("custom.stance") + 1;
			if (newEnabledState > 2) {
				newEnabledState = 0;
			}
			mod.getConfig().setProperty("custom.stance", newEnabledState);
			mod.saveConfig();
			par1GuiButton.displayString = "Walking stance: " + getStance();
			
			mod.reloadEverything(false);
		} else if (par1GuiButton.id == 220) {
			try {
				(new BlockReport(mod)).generateUnknownReport().printResults("presencefootsteps/report.txt");
			} catch (Exception e) {
				mod.getChatter().printChat(EnumChatFormatting.RED, "Failed to generate custom block report: " + e.getMessage());
			}
		}
		
		/*
			else if (par1GuiButton.id == 212)
			{
			mc.displayGuiScreen(parentScreen);
			mod.stopRunning();
			}*/
		
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
		//drawDefaultBackground();
		drawGradientRect(0, 0, width, height, 0xC0000000, 0x60000000);
		
		drawCenteredString(fontRendererObj, screenTitle, width / 2, 8, 0xffffff);
		
		if (!mod.hasResourcePacksLoaded()) {
			if (mod.hasNonethelessResourcePacksInstalled()) {
				drawCenteredString(fontRendererObj, "Your Presence Footsteps Resource Pack isn't enabled yet!", width / 2, 10 + 22 * 6 - 40 + 20, 0xff0000);
				drawCenteredString(fontRendererObj, "Activate it in the Minecraft Options menu for it to run.", width / 2, 10 + 22 * 6 - 40 + 28, 0xff0000);
			} else {
				drawCenteredString(fontRendererObj, "You don't have any Presence Footsteps Resource Pack installed!", width / 2, 10 + 22 * 6 - 40 + 20, 0xff0000);
				drawCenteredString(fontRendererObj, "Put the Resource Pack in the resourcepacks/ folder.", width / 2, 10 + 22 * 6 - 40 + 28, 0xff0000);
			}
		}
		
		super.drawScreen(par1, par2, par3);
		
	}
	
}
