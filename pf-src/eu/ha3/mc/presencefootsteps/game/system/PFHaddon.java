package eu.ha3.mc.presencefootsteps.game.system;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import org.apache.commons.lang3.ArrayUtils;

import eu.ha3.easy.EdgeModel;
import eu.ha3.easy.EdgeTrigger;
import eu.ha3.mc.convenience.Ha3HoldActions;
import eu.ha3.mc.convenience.Ha3KeyHolding;
import eu.ha3.mc.convenience.Ha3KeyManager;
import eu.ha3.mc.convenience.Ha3StaticUtilities;
import eu.ha3.mc.haddon.Identity;
import eu.ha3.mc.haddon.OperatorCaster;
import eu.ha3.mc.haddon.implem.HaddonIdentity;
import eu.ha3.mc.haddon.implem.HaddonImpl;
import eu.ha3.mc.haddon.supporting.SupportsFrameEvents;
import eu.ha3.mc.haddon.supporting.SupportsKeyEvents;
import eu.ha3.mc.haddon.supporting.SupportsTickEvents;
import eu.ha3.mc.presencefootsteps.game.user.PFGuiMenu;
import eu.ha3.mc.presencefootsteps.log.PFLog;
import eu.ha3.mc.presencefootsteps.mcpackage.implem.AcousticsManager;
import eu.ha3.mc.presencefootsteps.mcpackage.implem.BasicPrimitiveMap;
import eu.ha3.mc.presencefootsteps.mcpackage.implem.LegacyCapableBlockMap;
import eu.ha3.mc.presencefootsteps.mcpackage.implem.NormalVariator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.BlockMap;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.PrimitiveMap;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Variator;
import eu.ha3.mc.presencefootsteps.parsers.JasonAcoustics_Engine0;
import eu.ha3.mc.presencefootsteps.parsers.PropertyBlockMap_Engine0;
import eu.ha3.mc.presencefootsteps.parsers.PropertyPrimitiveMap_Engine0;
import eu.ha3.mc.quick.chat.ChatColorsSimple;
import eu.ha3.mc.quick.chat.Chatter;
import eu.ha3.mc.quick.keys.KeyWatcher;
import eu.ha3.mc.quick.update.NotifiableHaddon;
import eu.ha3.mc.quick.update.UpdateNotifier;
import eu.ha3.util.property.simple.ConfigProperty;
import eu.ha3.util.property.simple.InputStreamConfigProperty;

/* x-placeholder-wtfplv2 */

public class PFHaddon extends HaddonImpl
	implements SupportsFrameEvents, SupportsTickEvents, IResourceManagerReloadListener, NotifiableHaddon,
	Ha3HoldActions, SupportsKeyEvents
{
	// Identity
	protected final String NAME = "Presence Footsteps";
	protected final int VERSION = 5;
	protected final String FOR = "1.7.10";
	protected final String ADDRESS = "http://presencefootsteps.ha3.eu";
	protected final Identity identity = new HaddonIdentity(this.NAME, this.VERSION, this.FOR, this.ADDRESS);
	
	// NotifiableHaddon and UpdateNotifier
	private ConfigProperty config; // Can't be final
	private final Chatter chatter = new Chatter(this, this.NAME);
	private UpdateNotifier updateNotifier;
	
	// Meta
	private File presenceDir;
	private EdgeTrigger debugButton;
	private long pressedOptionsTime;
	
	// System
	private PFResourcePackDealer dealer = new PFResourcePackDealer();
	private PFIsolator isolator;
	
	// Binds
	private final int keyBindDefaultCode = 0;
	private KeyBinding keyBindingMain;
	private final KeyWatcher watcher = new KeyWatcher(this);
	private final Ha3KeyManager keyManager = new Ha3KeyManager();
	
	// Use once
	private boolean firstTickPassed;
	private boolean mlpDetectedFirst;
	private boolean hasResourcePacks;
	private boolean hasDisabledResourcePacks;
	private boolean hasResourcePacks_FixMe;
	private int tickRound;
	
	@Override
	public void onLoad()
	{
		this.updateNotifier = new UpdateNotifier(this, "http://q.mc.ha3.eu/query/pf-litemod-version.json?ver=%d");
		
		util().registerPrivateSetter(
			"Entity_nextStepDistance", Entity.class, -1, "nextStepDistance", "field_70150_b", "d");
		util().registerPrivateGetter("isJumping", EntityLivingBase.class, -1, "isJumping", "field_70703_bu", "bd");
		
		this.presenceDir = new File(util().getModsFolder(), "presencefootsteps/");
		
		if (!this.presenceDir.exists())
		{
			this.presenceDir.mkdirs();
		}
		
		this.debugButton = new EdgeTrigger(new EdgeModel() {
			@Override
			public void onTrueEdge()
			{
				PFHaddon.this.pressedOptionsTime = System.currentTimeMillis();
				
				PFLog.setDebugEnabled(true);
				reloadEverything(false);
			}
			
			@Override
			public void onFalseEdge()
			{
			}
		});
		
		// Config is loaded here
		reloadEverything(false);
		
		if (isInstalledMLP())
		{
			if (getConfig().getBoolean("mlp.detected") == false)
			{
				getConfig().setProperty("mlp.detected", true);
				getConfig().setProperty("custom.stance", 1);
				saveConfig();
				
				this.mlpDetectedFirst = true;
			}
		}
		
		this.keyBindingMain = new KeyBinding("Presence Footsteps", 0, "key.categories.misc");
		Minecraft.getMinecraft().gameSettings.keyBindings =
			ArrayUtils.addAll(Minecraft.getMinecraft().gameSettings.keyBindings, this.keyBindingMain);
		this.keyBindingMain.setKeyCode(getConfig().getInteger("key.code"));
		KeyBinding.resetKeyBindingArrayAndHash();
		
		this.watcher.add(this.keyBindingMain);
		this.keyManager.addKeyBinding(this.keyBindingMain, new Ha3KeyHolding(this, 7));
		
		// Hooking
		IResourceManager resMan = Minecraft.getMinecraft().getResourceManager();
		if (resMan instanceof IReloadableResourceManager)
		{
			((IReloadableResourceManager) resMan).registerReloadListener(this);
		}
		
		((OperatorCaster) op()).setTickEnabled(true);
		((OperatorCaster) op()).setFrameEnabled(true);
	}
	
	public void reloadEverything(boolean nested)
	{
		this.isolator = new PFIsolator(this);
		
		reloadConfig();
		
		List<ResourcePackRepository.Entry> repo = this.dealer.findResourcePacks();
		if (repo.size() == 0)
		{
			PFLog.log("Presence Footsteps didn't find any compatible resource pack.");
			this.hasResourcePacks = false;
			this.hasDisabledResourcePacks = this.dealer.findDisabledResourcePacks().size() > 0;
			
			this.isolator.setGenerator(null);
			
			return;
		}
		this.hasResourcePacks = true;
		this.hasDisabledResourcePacks = false;
		
		for (ResourcePackRepository.Entry pack : repo)
		{
			PFLog.debug("Will load: " + pack.getResourcePackName());
		}
		
		reloadBlockMap(repo);
		reloadPrimitiveMap(repo);
		reloadAcoustics(repo);
		this.isolator.setSolver(new PFSolver(this.isolator));
		reloadVariator(repo);
		
		this.isolator.setGenerator(getConfig().getInteger("custom.stance") == 0
			? new PFReaderH(this.isolator, util()) : new PFReaderQP(this.isolator, util()));
	}
	
	private void reloadConfig()
	{
		this.config = new ConfigProperty();
		this.updateNotifier.fillDefaults(this.config);
		this.config.setProperty("user.volume.0-to-100", 70);
		this.config.setProperty("mlp.detected", false);
		this.config.setProperty("custom.stance", 0);
		this.config.setProperty("key.code", this.keyBindDefaultCode);
		this.config.commit();
		
		boolean fileExisted = new File(this.presenceDir, "userconfig.cfg").exists();
		
		try
		{
			this.config.setSource(new File(this.presenceDir, "userconfig.cfg").getCanonicalPath());
			this.config.load();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException("Error caused config not to work: " + e.getMessage());
		}
		
		if (!fileExisted)
		{
			this.config.save();
		}
		
		this.updateNotifier.loadConfig(this.config);
	}
	
	private void reloadVariator(List<ResourcePackRepository.Entry> repo)
	{
		Variator var = new NormalVariator();
		
		int working = 0;
		for (ResourcePackRepository.Entry pack : repo)
		{
			try
			{
				InputStreamConfigProperty config = new InputStreamConfigProperty();
				config.loadStream(this.dealer.openVariator(pack.getResourcePack()));
				
				var.loadConfig(config);
				working = working + 1;
			}
			catch (Exception e)
			{
				PFLog.debug("No variator found in " + pack.getResourcePackName() + ": " + e.getMessage());
			}
		}
		if (working == 0)
		{
			PFLog.log("No variators found in " + repo.size() + " packs!");
		}
		
		this.isolator.setVariator(var);
	}
	
	private void reloadBlockMap(List<ResourcePackRepository.Entry> repo)
	{
		BlockMap blockMap = new LegacyCapableBlockMap();
		
		int working = 0;
		for (ResourcePackRepository.Entry pack : repo)
		{
			try
			{
				InputStreamConfigProperty blockSound = new InputStreamConfigProperty();
				blockSound.loadStream(this.dealer.openBlockMap(pack.getResourcePack()));
				
				new PropertyBlockMap_Engine0().setup(blockSound, blockMap);
				working = working + 1;
			}
			catch (IOException e)
			{
				PFLog.debug("No blockmap found in " + pack.getResourcePackName() + ": " + e.getMessage());
			}
		}
		if (working == 0)
		{
			PFLog.log("No blockmaps found in " + repo.size() + " packs!");
		}
		
		this.isolator.setBlockMap(blockMap);
	}
	
	private void reloadPrimitiveMap(List<ResourcePackRepository.Entry> repo)
	{
		PrimitiveMap primitiveMap = new BasicPrimitiveMap();
		
		int working = 0;
		for (ResourcePackRepository.Entry pack : repo)
		{
			try
			{
				InputStreamConfigProperty primitiveSound = new InputStreamConfigProperty();
				primitiveSound.loadStream(this.dealer.openPrimitiveMap(pack.getResourcePack()));
				
				new PropertyPrimitiveMap_Engine0().setup(primitiveSound, primitiveMap);
				working = working + 1;
			}
			catch (IOException e)
			{
				PFLog.debug("No primitivemap found in " + pack.getResourcePackName() + ": " + e.getMessage());
			}
		}
		if (working == 0)
		{
			PFLog.log("No blockmaps found in " + repo.size() + " packs!");
		}
		
		this.isolator.setPrimitiveMap(primitiveMap);
	}
	
	private void reloadAcoustics(List<ResourcePackRepository.Entry> repo)
	{
		AcousticsManager acoustics = new AcousticsManager(this.isolator);
		
		int working = 0;
		for (ResourcePackRepository.Entry pack : repo)
		{
			try
			{
				String jasonString =
					new Scanner(this.dealer.openAcoustics(pack.getResourcePack())).useDelimiter("\\Z").next();
				
				new JasonAcoustics_Engine0("").parseJSON(jasonString, acoustics);
				working = working + 1;
			}
			catch (IOException e)
			{
				PFLog.debug("No acoustics found in " + pack.getResourcePackName() + ": " + e.getMessage());
			}
		}
		if (working == 0)
		{
			PFLog.log("No blockmaps found in " + repo.size() + " packs!");
		}
		
		this.isolator.setAcoustics(acoustics);
		this.isolator.setSoundPlayer(new UserConfigSoundPlayerWrapper(acoustics, this.config));
		this.isolator.setDefaultStepPlayer(acoustics);
	}
	
	//
	
	private boolean isInstalledMLP()
	{
		return Ha3StaticUtilities.classExists("com.minelittlepony.minelp.Pony", this);
	}
	
	//
	
	@Override
	public void onFrame(float semi)
	{
		EntityPlayer ply = Minecraft.getMinecraft().thePlayer;
		
		if (ply == null)
			return;
		
		this.isolator.onFrame();
		
		boolean keysDown = util().areKeysDown(29, 42, 33);
		this.debugButton.signalState(keysDown); // CTRL SHIFT F
		if (keysDown && System.currentTimeMillis() - this.pressedOptionsTime > 1000)
		{
			displayMenu();
		}
		
		try
		{
			//nextStepDistance
			util().setPrivate(ply, "Entity_nextStepDistance", Integer.MAX_VALUE);
			//util().setPrivateValueLiteral(Entity.class, ply, "c", 37, Integer.MAX_VALUE);
			//util().setPrivateValueLiteral(Entity.class, ply, "c", 37, 0);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		if (!this.firstTickPassed)
		{
			this.firstTickPassed = true;
			this.updateNotifier.attempt();
			if (this.mlpDetectedFirst)
			{
				this.chatter
					.printChat(
						ChatColorsSimple.COLOR_TEAL,
						"Mine Little Pony has been detected! ",
						ChatColorsSimple.COLOR_WHITE,
						"4-legged mode has been enabled, which will make running sound like galloping amongst other things. ",
						ChatColorsSimple.COLOR_GRAY,
						"You can hold down for 1 second the combination LEFT CTRL + LEFT SHIFT + F to disable it.");
			}
			
			if (!this.hasResourcePacks)
			{
				this.hasResourcePacks_FixMe = true;
				if (this.hasDisabledResourcePacks)
				{
					this.chatter.printChat(ChatColorsSimple.COLOR_RED, "Resource Pack not enabled yet!");
					this.chatter.printChatShort(ChatColorsSimple.COLOR_WHITE, "You need to activate "
						+ "\"Presence Footsteps Resource Pack\" in the Minecraft Options menu for it to run.");
				}
				else
				{
					this.chatter.printChat(ChatColorsSimple.COLOR_RED, "Resource Pack missing from resourcepacks/!");
					this.chatter.printChatShort(
						ChatColorsSimple.COLOR_WHITE,
						"You may have forgotten to put the Resource Pack file into your resourcepacks/ folder.");
				}
				if (getConfig().getInteger("key.code") == this.keyBindDefaultCode)
				{
					this.chatter.printChatShort(
						ChatColorsSimple.COLOR_GRAY,
						"There is also a Presence Footsteps menu key in the Controls menu.");
				}
			}
		}
		if (this.hasResourcePacks_FixMe && this.hasResourcePacks)
		{
			this.hasResourcePacks_FixMe = false;
			this.chatter.printChat(ChatColorsSimple.COLOR_BRIGHTGREEN, "It should work now!");
		}
	}
	
	private void displayMenu()
	{
		if (util().isCurrentScreen(null))
		{
			Minecraft.getMinecraft().displayGuiScreen(new PFGuiMenu((GuiScreen) util().getCurrentScreen(), this));
			PFLog.setDebugEnabled(false);
		}
	}
	
	public boolean hasResourcePacksLoaded()
	{
		return this.hasResourcePacks;
	}
	
	public boolean hasNonethelessResourcePacksInstalled()
	{
		return this.hasDisabledResourcePacks;
	}
	
	@Override
	public ConfigProperty getConfig()
	{
		return this.config;
	}
	
	@Override
	public void saveConfig()
	{
		// If there were changes...
		if (this.config.commit())
		{
			PFLog.log("Saving configuration...");
			
			// Write changes on disk.
			this.config.save();
		}
	}
	
	@Override
	public void onResourceManagerReload(IResourceManager var1)
	{
		PFLog.log("Resource Pack reload detected...");
		reloadEverything(false);
	}
	
	@Override
	public Chatter getChatter()
	{
		return this.chatter;
	}
	
	@Override
	public Identity getIdentity()
	{
		return this.identity;
	}
	
	@Override
	public void beginPress()
	{
		displayMenu();
	}
	
	@Override
	public void endPress()
	{
	}
	
	@Override
	public void shortPress()
	{
	}
	
	@Override
	public void beginHold()
	{
	}
	
	@Override
	public void endHold()
	{
	}
	
	@Override
	public void onKey(KeyBinding event)
	{
		this.keyManager.handleKeyDown(event);
	}
	
	@Override
	public void onTick()
	{
		if (this.tickRound == 0)
		{
			int keyCode = this.keyBindingMain.getKeyCode();
			if (keyCode != this.config.getInteger("key.code"))
			{
				PFLog.log("Key binding changed. Saving...");
				this.config.setProperty("key.code", keyCode);
				saveConfig();
			}
		}
		this.watcher.onTick();
		this.keyManager.handleRuntime();
		this.tickRound = (this.tickRound + 1) % 100;
	}
}
