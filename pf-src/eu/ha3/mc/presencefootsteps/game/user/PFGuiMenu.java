package eu.ha3.mc.presencefootsteps.game.user;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import eu.ha3.mc.gui.HDisplayStringProvider;
import eu.ha3.mc.gui.HGuiSliderControl;
import eu.ha3.mc.gui.HSliderListener;
import eu.ha3.mc.presencefootsteps.game.system.PFHaddon;

/* x-placeholder-wtfplv2 */

public class PFGuiMenu extends GuiScreen
{
	/**
	 * A reference to the screen object that created this. Used for navigating
	 * between screens.
	 */
	private GuiScreen parentScreen;
	
	/** The title string that is displayed in the top-center of the screen. */
	protected String screenTitle;
	
	private PFHaddon mod;
	
	/** The ID of the button that has been pressed. */
	private int buttonId;
	
	private final int IDS_PER_PAGE = 5;
	
	public PFGuiMenu(GuiScreen par1GuiScreen, PFHaddon mod)
	{
		this.screenTitle = "Presence Footsteps Configuration";
		this.buttonId = -1;
		this.parentScreen = par1GuiScreen;
		this.mod = mod;
	}
	
	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		final int _GAP = 2;
		final int _UNIT = 20;
		final int _WIDTH = 155 * 2;
		
		final int _MIX = _GAP + _UNIT;
		
		final int _LEFT = this.width / 2 - _WIDTH / 2;
		final int _RIGHT = this.width / 2 + _WIDTH / 2;
		
		int id = 0;
		
		{
			HGuiSliderControl sliderControl =
				new HGuiSliderControl(id, _LEFT, _MIX, _WIDTH, _UNIT, "", this.mod.getConfig().getInteger(
					"user.volume.0-to-100") / 100f);
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
					
					PFGuiMenu.this.mod.getConfig().setProperty("user.volume.0-to-100", valueSnappedInt);
					slider.updateDisplayString();
				}
				
				@Override
				public void sliderPressed(HGuiSliderControl hGuiSliderControl)
				{
				}
				
				@Override
				public void sliderReleased(HGuiSliderControl hGuiSliderControl)
				{
				}
			});
			sliderControl.setDisplayStringProvider(new HDisplayStringProvider() {
				@Override
				public String provideDisplayString()
				{
					return "Global Volume Control: "
						+ PFGuiMenu.this.mod.getConfig().getInteger("user.volume.0-to-100") + "%";
				}
			});
			sliderControl.updateDisplayString();
			
			this.buttonList.add(sliderControl);
			id++;
			
		}
		
		final int _ASPLIT = 2;
		final int _AWID = _WIDTH / _ASPLIT - _GAP * (_ASPLIT - 1) / 2;
		
		final int _SEPARATOR = 10;
		
		if (this.mod.getConfig().getBoolean("mlp.detected") || this.mod.getConfig().getBoolean("mlp.enabled"))
		{
			this.buttonList.add(new GuiButton(210, _LEFT, _SEPARATOR + _MIX * 2, _WIDTH, _UNIT, this.mod
				.getConfig().getBoolean("mlp.enabled") ? "Walk with 4 legs: ON" : "Walk with 4 legs: OFF"));
		}
		
		final int _TURNOFFWIDTH = _WIDTH / 5;
		
		this.buttonList.add(new GuiButton(200, _LEFT + _MIX, _SEPARATOR + _MIX * (this.IDS_PER_PAGE + 4), _WIDTH
			- _MIX * 2 - _GAP - _TURNOFFWIDTH, _UNIT, "Done"));
		
		/*this.buttonList.add(new GuiButton(212, _RIGHT - _TURNOFFWIDTH - _MIX, _SEPARATOR
			+ _MIX * (this.IDS_PER_PAGE + 4), _TURNOFFWIDTH, _UNIT, "Turn Off"));*/
	}
	
	/**
	 * Fired when a control is clicked. This is the equivalent of
	 * ActionListener.actionPerformed(ActionEvent e).
	 */
	@Override
	protected void actionPerformed(GuiButton par1GuiButton)
	{
		if (par1GuiButton.id == 200)
		{
			// This triggers onGuiClosed
			this.mc.displayGuiScreen(this.parentScreen);
		}
		else if (par1GuiButton.id == 210)
		{
			boolean newEnabledState = !this.mod.getConfig().getBoolean("mlp.enabled");
			this.mod.getConfig().setProperty("mlp.enabled", newEnabledState);
			par1GuiButton.displayString = newEnabledState ? "Walk with 4 legs: ON" : "Walk with 4 legs: OFF";
			this.mod.saveConfig();
			
			this.mod.reloadEverything(false);
		}/*
			else if (par1GuiButton.id == 212)
			{
			this.mc.displayGuiScreen(this.parentScreen);
			this.mod.stopRunning();
			}*/
		
	}
	
	private void aboutToClose()
	{
		this.mod.saveConfig();
	}
	
	@Override
	public void onGuiClosed()
	{
		aboutToClose();
	}
	
	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked(int par1, int par2, int par3)
	{
		if (this.buttonId >= 0)
		{
		}
		else
		{
			super.mouseClicked(par1, par2, par3);
		}
	}
	
	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int par1, int par2, float par3)
	{
		// XXX 2014-01-03 : 1.7.2 unsure
		System.err.println("FIXME: No background");
		//drawDefaultBackground();
		drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 8, 0xffffff);
		
		super.drawScreen(par1, par2, par3);
		
	}
	
}
