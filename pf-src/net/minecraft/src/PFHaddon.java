package net.minecraft.src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import eu.ha3.easy.EdgeModel;
import eu.ha3.easy.EdgeTrigger;
import eu.ha3.mc.haddon.PrivateAccessException;
import eu.ha3.mc.haddon.SupportsFrameEvents;
import eu.ha3.mc.presencefootsteps.mcpackage.implem.AcousticsManager;
import eu.ha3.mc.presencefootsteps.mcpackage.implem.BasicBlockMap;
import eu.ha3.mc.presencefootsteps.mcpackage.implem.BasicPrimitiveMap;
import eu.ha3.mc.presencefootsteps.mcpackage.implem.NormalVariator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.BlockMap;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.PrimitiveMap;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Variator;
import eu.ha3.mc.presencefootsteps.mod.UpdateNotifier;
import eu.ha3.mc.presencefootsteps.mod.UserConfigSoundPlayerWrapper;
import eu.ha3.mc.presencefootsteps.parsers.JasonAcoustics_Engine0;
import eu.ha3.mc.presencefootsteps.parsers.PropertyBlockMap_Engine0;
import eu.ha3.mc.presencefootsteps.parsers.PropertyPrimitiveMap_Engine0;
import eu.ha3.util.property.simple.ConfigProperty;

/*
            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
                    Version 2, December 2004 

 Copyright (C) 2004 Sam Hocevar <sam@hocevar.net> 

 Everyone is permitted to copy and distribute verbatim or modified 
 copies of this license document, and changing it is allowed as long 
 as the name is changed. 

            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION 

  0. You just DO WHAT THE FUCK YOU WANT TO. 
*/

public class PFHaddon extends HaddonImpl implements SupportsFrameEvents
{
	public static final int VERSION = 0;
	public static final String FOR = "1.6.2 (Special build)";
	
	private File presenceDir;
	private File packsFolder;
	
	private PFCacheRegistry cache;
	private EdgeTrigger debugButton;
	private static boolean isDebugEnabled;
	
	private ConfigProperty config;
	
	private File currentPackFolder;
	private PFIsolator isolator;
	
	private UpdateNotifier update;
	
	private static String DEFAULT_PACK_NAME = "pf_presence";
	
	private List<ResourcePack> resourcePacks;
	private boolean firstTickPassed;
	
	private long pressedOptionsTime;
	
	@SuppressWarnings("unchecked")
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
		this.cache = new PFCacheRegistry();
		
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
		
		try
		{
			this.resourcePacks =
				(List<ResourcePack>) util().getPrivateValueLiteral(Minecraft.class, Minecraft.getMinecraft(), "aq", 63);
			for (File file : new File(this.presenceDir, "packs/").listFiles())
			{
				if (file.isDirectory())
				{
					PFHaddon.log("Adding resource pack at " + file.getAbsolutePath());
					this.resourcePacks.add(new FolderResourcePack(file));
				}
			}
		}
		catch (PrivateAccessException e)
		{
			e.printStackTrace();
		}
		
		reloadEverything(false);
		
		manager().hookFrameEvents(true);
		
	}
	
	private void reloadEverything(boolean nested)
	{
		this.isolator = new PFIsolator(this);
		
		// Sets up the pack
		reloadConfig();
		
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
		
		reloadBlockMapFromFile();
		reloadPrimitiveMapFromFile();
		reloadAcousticsFromFile();
		this.isolator.setSolver(new PFSolver(this.isolator));
		reloadVariatorFromFile();
		loadSoundsFromPack(this.currentPackFolder);
		
		this.isolator.setGenerator(new PFReaderH(this.isolator));
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
		
		this.currentPackFolder =
			new File(this.packsFolder, (this.config.getAllProperties().containsKey("user.packname.r0")
				? this.config.getString("user.packname.r0") : PFHaddon.DEFAULT_PACK_NAME)
				+ "/");
	}
	
	private void reloadVariatorFromFile()
	{
		Variator var = new NormalVariator();
		
		File configFile = new File(this.currentPackFolder, "variator.cfg");
		if (configFile.exists())
		{
			try
			{
				ConfigProperty config = new ConfigProperty();
				config.setSource(configFile.getCanonicalPath());
				config.load();
				
				var.loadConfig(config);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				PFHaddon.log("Loading default configuration failed: " + e.getMessage());
			}
		}
		
		this.isolator.setVariator(var);
	}
	
	private void reloadBlockMapFromFile()
	{
		BlockMap blockMap = new BasicBlockMap();
		
		try
		{
			ConfigProperty blockSound = new ConfigProperty();
			blockSound.setSource(new File(this.currentPackFolder, "blockmap.cfg").getCanonicalPath());
			blockSound.load();
			
			new PropertyBlockMap_Engine0().setup(blockSound, blockMap);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			PFHaddon.log("Loading default blockmap failed: " + e.getMessage());
		}
		
		this.isolator.setBlockMap(blockMap);
	}
	
	private void reloadPrimitiveMapFromFile()
	{
		PrimitiveMap primitiveMap = new BasicPrimitiveMap();
		
		try
		{
			ConfigProperty primitiveSound = new ConfigProperty();
			primitiveSound.setSource(new File(this.currentPackFolder, "primitivemap.cfg").getCanonicalPath());
			primitiveSound.load();
			
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
			String jasonString =
				new Scanner(new File(this.currentPackFolder, "acoustics.json")).useDelimiter("\\Z").next();
			
			new JasonAcoustics_Engine0("").parseJSON(jasonString, acoustics);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			PFHaddon.log("Loading default acoustics failed: " + e.getMessage());
		}
		
		this.isolator.setAcoustics(acoustics);
		this.isolator.setSoundPlayer(new UserConfigSoundPlayerWrapper(acoustics, this.config));
		this.isolator.setDefaultStepPlayer(acoustics);
	}
	
	//
	
	/*private boolean isInstalledMLP()
	{
		return Ha3StaticUtilities.classExists("Pony", this)
			|| Ha3StaticUtilities.classExists("net.minecraft.src.Pony", this);
	}*/
	
	private void loadSoundsFromPack(File pack)
	{
		File soundFolder = new File(pack, "assets/minecraft/sound/");
		if (soundFolder.exists())
		{
			loadResource(soundFolder, "");
		}
	}
	
	//
	
	@Override
	public void onFrame(float semi)
	{
		EntityPlayer ply = manager().getMinecraft().thePlayer;
		
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
			
		}
	}
	
	/**
	 * Loads a resource and passes it to Minecraft to install.
	 */
	private void loadResource(File par1File, String root)
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
	}
	
	public ConfigProperty getConfig()
	{
		return this.config;
	}
	
	public void printChat(Object... args)
	{
		printChat(new Object[] { Ha3Utility.COLOR_WHITE, "Presence Footsteps: " }, args);
	}
	
	public void printChatShort(Object... args)
	{
		printChat(new Object[] { Ha3Utility.COLOR_WHITE, "" }, args);
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
	
}
