package net.minecraft.src;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import eu.ha3.easy.EdgeModel;
import eu.ha3.easy.EdgeTrigger;
import eu.ha3.mc.convenience.Ha3StaticUtilities;
import eu.ha3.mc.haddon.implem.HaddonImpl;
import eu.ha3.mc.haddon.supporting.SupportsFrameEvents;
import eu.ha3.mc.presencefootsteps.mcpackage.implem.AcousticsManager;
import eu.ha3.mc.presencefootsteps.mcpackage.implem.BasicPrimitiveMap;
import eu.ha3.mc.presencefootsteps.mcpackage.implem.LegacyCapableBlockMap;
import eu.ha3.mc.presencefootsteps.mcpackage.implem.NormalVariator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.BlockMap;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.PrimitiveMap;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Variator;
import eu.ha3.mc.presencefootsteps.mod.UpdateNotifier;
import eu.ha3.mc.presencefootsteps.mod.UserConfigSoundPlayerWrapper;
import eu.ha3.mc.presencefootsteps.modplants.ResourcePackDealer;
import eu.ha3.mc.presencefootsteps.parsers.JasonAcoustics_Engine0;
import eu.ha3.mc.presencefootsteps.parsers.PropertyBlockMap_Engine0;
import eu.ha3.mc.presencefootsteps.parsers.PropertyPrimitiveMap_Engine0;
import eu.ha3.mc.quick.ChatColorsSimple;
import eu.ha3.util.property.simple.ConfigProperty;
import eu.ha3.util.property.simple.InputStreamConfigProperty;

/* x-placeholder-wtfplv2 */

public class PFHaddon extends HaddonImpl implements SupportsFrameEvents
{
	public static final String NAME = "Presence Footsteps";
	public static final int VERSION = 1;
	public static final String FOR = "1.6.2";
	public static final String ADDRESS = "http://presencefootsteps.ha3.eu";
	
	private File presenceDir;
	private File packsFolder;
	
	private EdgeTrigger debugButton;
	private static boolean isDebugEnabled;
	
	private ConfigProperty config;
	
	private PFIsolator isolator;
	
	private UpdateNotifier update;
	
	private static String DEFAULT_PACK_NAME = "pf_presence";
	
	private ResourcePackDealer dealer;
	private IResourcePack currentPack;
	private boolean firstTickPassed;
	private boolean mlpDetectedFirst;
	
	private long pressedOptionsTime;
	
	@Override
	public void onLoad()
	{
		this.presenceDir = new File(util().getModsFolder(), "presencefootsteps/");
		this.packsFolder = new File(this.presenceDir, "packs/");
		
		if (!this.presenceDir.exists())
		{
			this.presenceDir.mkdirs();
		}
		if (!this.packsFolder.exists())
		{
			this.packsFolder.mkdirs();
		}
		//this.cache = new PFCacheRegistry();
		
		this.update = new UpdateNotifier(this);
		
		this.debugButton = new EdgeTrigger(new EdgeModel() {
			@Override
			public void onTrueEdge()
			{
				PFHaddon.this.pressedOptionsTime = System.currentTimeMillis();
				
				setDebugEnabled(true);
				reloadEverything(false);
			}
			
			@Override
			public void onFalseEdge()
			{
			}
		});
		
		/*if (isInstalledMLP())
		{
			this.generator = new PFReaderMLP(this.isolator);
		}
		else
		{
			this.generator = new PFReader4P(this.isolator);
		}*/
		
		// XXX 2014-01-03 : 1.7.2 UNSURE
		/*this.resourcePacks = Minecraft.getMinecraft().getResourcePackRepository();
		for (File file : new File(this.presenceDir, "packs/").listFiles())
		{
			if (file.isDirectory())
			{
				PFHaddon.log("Adding resource pack at " + file.getAbsolutePath());
				this.resourcePacks.getRepositoryEntries().add(new FolderResourcePack(file));
			}
		}
		this.resourcePacks.updateRepositoryEntriesAll();*/
		
		this.dealer = new ResourcePackDealer();
		
		reloadEverything(false);
		caster().setFrameEnabled(true);
		
	}
	
	public void reloadEverything(boolean nested)
	{
		this.isolator = new PFIsolator(this);
		
		// Sets up the pack
		reloadConfig();
		
		@SuppressWarnings("unchecked")
		List<ResourcePackRepository.Entry> repo =
			Minecraft.getMinecraft().getResourcePackRepository().getRepositoryEntries();
		
		ResourcePackRepository.Entry lastPackFound = null;
		for (ResourcePackRepository.Entry pack : repo)
		{
			System.err.println(pack.getResourcePackName()
				+ " is " + (this.dealer.checkoutPresencePack(pack) ? "" : "NOT ") + "a PF pack");
			if (this.dealer.checkoutPresencePack(pack))
			{
				lastPackFound = pack;
			}
		}
		
		if (lastPackFound == null)
		{
			PFHaddon.log("Presence Footsteps didn't find any compatible resource pack.");
			return;
		}
		this.currentPack = lastPackFound.getResourcePack();
		
		/*
		if (!this.currentPackFolder.exists())
		{
			PFHaddon.log("The pack '" + this.currentPackFolder.getPath() + "'does not exist!");
			if (!nested)
			{
				this.currentPackFolder = new File(this.packsFolder, PFHaddon.DEFAULT_PACK_NAME + "/");
				PFHaddon.log("The pack '" + this.currentPackFolder.getPath() + "'does not exist!");
				
				reloadEverything(true);
			}
			else
				throw new RuntimeException(
					"Presence Footsteps cannot run because the default custom pack does not exist in the "
						+ new File(this.packsFolder, PFHaddon.DEFAULT_PACK_NAME + "/").getAbsolutePath() + " folder.");
		}
		*/
		
		if (isInstalledMLP())
		{
			if (getConfig().getBoolean("mlp.detected") == false)
			{
				getConfig().setProperty("mlp.detected", true);
				getConfig().setProperty("mlp.enabled", true);
				saveConfig();
				
				this.mlpDetectedFirst = true;
			}
		}
		
		reloadBlockMapFromFile();
		reloadPrimitiveMapFromFile();
		reloadAcousticsFromFile();
		this.isolator.setSolver(new PFSolver(this.isolator));
		reloadVariatorFromFile();
		//loadSoundsFromPack(this.currentPackFolder);
		
		this.isolator.setGenerator(!getConfig().getBoolean("mlp.enabled")
			? new PFReaderH(this.isolator) : new PFReaderQP(this.isolator));
	}
	
	private void reloadConfig()
	{
		this.config = new ConfigProperty();
		this.config.setProperty("user.volume.0-to-100", 70);
		this.config.setProperty("user.packname.r0", PFHaddon.DEFAULT_PACK_NAME);
		this.config.setProperty("update_found.enabled", true);
		this.config.setProperty("update_found.version", PFHaddon.VERSION);
		this.config.setProperty("update_found.display.remaining.value", 0);
		this.config.setProperty("update_found.display.count.value", 3);
		this.config.setProperty("mlp.detected", false);
		this.config.setProperty("mlp.enabled", false);
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
		
		this.update.loadConfig(this.config);
		
		//this.currentPackFolder =
		//	new File(this.packsFolder, (this.config.getAllProperties().containsKey("user.packname.r0")
		//		? this.config.getString("user.packname.r0") : PFHaddon.DEFAULT_PACK_NAME)
		//		+ "/");
	}
	
	private void reloadVariatorFromFile()
	{
		Variator var = new NormalVariator();
		
		try
		{
			InputStreamConfigProperty config = new InputStreamConfigProperty();
			config.loadStream(this.dealer.openVariator(this.currentPack));
			
			var.loadConfig(config);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			PFHaddon.log("Loading default configuration failed: " + e.getMessage());
		}
		
		this.isolator.setVariator(var);
	}
	
	private void reloadBlockMapFromFile()
	{
		BlockMap blockMap = new LegacyCapableBlockMap();
		
		try
		{
			InputStreamConfigProperty blockSound = new InputStreamConfigProperty();
			blockSound.loadStream(this.dealer.openBlockMap(this.currentPack));
			
			new PropertyBlockMap_Engine0().setup(blockSound, blockMap);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			PFHaddon.log("Loading default blockmap failed: " + e.getMessage());
		}
		
		/*for (File file : this.currentPackFolder.listFiles())
		{
			if (file.getName().startsWith("blockmap_") && file.getName().endsWith(".cfg"))
			{
				try
				{
					ConfigProperty blockSound = new ConfigProperty();
					blockSound.setSource(file.getCanonicalPath());
					blockSound.load();
					
					if (blockSound.getAllProperties().containsKey("_classname"))
					{
						if (eu.ha3.mc.convenience.Ha3StaticUtilities.classExists(
							blockSound.getString("_classname"), this))
						{
							PFHaddon.debug("Loading mod ("
								+ blockSound.getString("_classname") + ") blockmap: " + file.getName());
							new PropertyBlockMap_Engine0().setup(blockSound, blockMap);
						}
						else
						{
							PFHaddon.debug("Skipping missing mod ("
								+ blockSound.getString("_classname") + ") blockmap: " + file.getName());
						}
					}
					else
					{
						PFHaddon.debug("Loading custom blockmap: " + file.getName());
						new PropertyBlockMap_Engine0().setup(blockSound, blockMap);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					PFHaddon.log("Loading custom blockmap failed: " + e.getMessage());
				}
			}
		}*/
		
		this.isolator.setBlockMap(blockMap);
	}
	
	private void reloadPrimitiveMapFromFile()
	{
		PrimitiveMap primitiveMap = new BasicPrimitiveMap();
		
		try
		{
			InputStreamConfigProperty primitiveSound = new InputStreamConfigProperty();
			primitiveSound.loadStream(this.dealer.openPrimitiveMap(this.currentPack));
			
			new PropertyPrimitiveMap_Engine0().setup(primitiveSound, primitiveMap);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			PFHaddon.log("Loading default primitivemap failed: " + e.getMessage());
		}
		
		this.isolator.setPrimitiveMap(primitiveMap);
	}
	
	private void reloadAcousticsFromFile()
	{
		AcousticsManager acoustics = new AcousticsManager(this.isolator);
		
		try
		{
			String jasonString = new Scanner(this.dealer.openAcoustics(this.currentPack)).useDelimiter("\\Z").next();
			
			new JasonAcoustics_Engine0("").parseJSON(jasonString, acoustics);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			PFHaddon.log("Loading default acoustics failed: " + e.getMessage());
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
	
	/*
	private void loadSoundsFromPack(File pack)
	{
		File soundFolder = new File(pack, "assets/minecraft/sound/");
		if (soundFolder.exists())
		{
			loadResource(soundFolder, "");
		}
	}
	*/
	
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
			if (util().isCurrentScreen(null))
			{
				Minecraft.getMinecraft().displayGuiScreen(new PFGuiMenu((GuiScreen) util().getCurrentScreen(), this));
				setDebugEnabled(false);
			}
		}
		
		try
		{
			//nextStepDistance
			util().setPrivateValueLiteral(Entity.class, ply, "c", 37, Integer.MAX_VALUE);
			//util().setPrivateValueLiteral(Entity.class, ply, "c", 37, 0);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		if (!this.firstTickPassed)
		{
			this.firstTickPassed = true;
			this.update.attempt();
			if (this.mlpDetectedFirst)
			{
				this
					.printChat(
						ChatColorsSimple.COLOR_TEAL,
						"Mine Little Pony has been detected! ",
						ChatColorsSimple.COLOR_WHITE,
						"4-legged mode has been enabled, which will make running sound like galloping amongst other things. ",
						ChatColorsSimple.COLOR_GRAY,
						"You can hold down for 1 second the combination LEFT CTRL + LEFT SHIFT + F to disable it.");
			}
		}
	}
	
	/**
	 * Loads a resource and passes it to Minecraft to install.
	 */
	/*private void loadResource(File par1File, String root)
	{
		File[] filesInThisDir = par1File.listFiles();
		int fileCount = filesInThisDir.length;
		
		for (int i = 0; i < fileCount; ++i)
		{
			File file = filesInThisDir[i];
			
			if (file.isDirectory())
			{
				loadResource(file, root + file.getName() + "/");
			}
			else
			{
				try
				{
					this.cache.cacheSound(root + file.getName());
				}
				catch (Exception var9)
				{
					log("Failed to add " + root + file.getName());
				}
			}
		}
	}*/
	
	public ConfigProperty getConfig()
	{
		return this.config;
	}
	
	public void printChat(Object... args)
	{
		printChat(new Object[] { ChatColorsSimple.COLOR_WHITE, "Presence Footsteps: " }, args);
	}
	
	public void printChatShort(Object... args)
	{
		printChat(new Object[] { ChatColorsSimple.COLOR_WHITE, "" }, args);
	}
	
	protected void printChat(final Object[] in, Object... args)
	{
		Object[] dest = new Object[in.length + args.length];
		System.arraycopy(in, 0, dest, 0, in.length);
		System.arraycopy(args, 0, dest, in.length, args.length);
		
		util().printChat(dest);
	}
	
	public static void log(String contents)
	{
		System.out.println("(PF) " + contents);
	}
	
	public static void setDebugEnabled(boolean enable)
	{
		isDebugEnabled = enable;
	}
	
	public static void debug(String contents)
	{
		if (!isDebugEnabled)
			return;
		
		System.out.println("(PF) " + contents);
	}
	
	public void saveConfig()
	{
		// If there were changes...
		if (this.config.commit())
		{
			PFHaddon.log("Saving configuration...");
			
			// Write changes on disk.
			this.config.save();
		}
	}
	
	@Override
	public String getName()
	{
		return PFHaddon.NAME;
	}
	
	@Override
	public String getVersion()
	{
		return "r" + PFHaddon.VERSION + " for " + PFHaddon.FOR;
	}
	
}
