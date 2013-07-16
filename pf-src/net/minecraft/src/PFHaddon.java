package net.minecraft.src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import eu.ha3.easy.EdgeModel;
import eu.ha3.easy.EdgeTrigger;
import eu.ha3.mc.haddon.SupportsFrameEvents;
import eu.ha3.mc.presencefootsteps.mcpackage.implem.AcousticsManager;
import eu.ha3.mc.presencefootsteps.mcpackage.implem.BasicBlockMap;
import eu.ha3.mc.presencefootsteps.mcpackage.implem.NormalVariator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.BlockMap;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Variator;
import eu.ha3.mc.presencefootsteps.mod.UpdateNotifier;
import eu.ha3.mc.presencefootsteps.mod.UserConfigSoundPlayerWrapper;
import eu.ha3.mc.presencefootsteps.parsers.JasonAcoustics_Engine0;
import eu.ha3.mc.presencefootsteps.parsers.PropertyBlockMap_Engine0;
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
	public static final String FOR = "1.6.2";
	
	private File presenceDir;
	private PFCacheRegistry cache;
	private EdgeTrigger debugButton;
	private static boolean isDebugEnabled;
	
	private ConfigProperty config;
	
	private File currentPackFolder;
	private PFIsolator isolator;
	
	private UpdateNotifier update;
	
	private static String DEFAULT_PACK_NAME = "pf_presence";
	private static String DEFAULT_PACK_FOLDER_PATH = DEFAULT_PACK_NAME + "/";
	
	@Override
	public void onLoad()
	{
		this.presenceDir = new File(util().getModsFolder(), "presencefootsteps/");
		if (!this.presenceDir.exists())
		{
			this.presenceDir.mkdirs();
		}
		this.cache = new PFCacheRegistry();
		
		this.debugButton = new EdgeTrigger(new EdgeModel() {
			@Override
			public void onTrueEdge()
			{
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
		
		reloadEverything(false);
		
		manager().hookFrameEvents(true);
		
		this.update = new UpdateNotifier(this);
		this.update.attempt();
		
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
				this.currentPackFolder = new File(util().getModsFolder(), PFHaddon.DEFAULT_PACK_FOLDER_PATH);
				PFHaddon.log("The pack '" + this.currentPackFolder.getPath() + "'does not exist!");
				
				reloadEverything(true);
			}
			else
				throw new RuntimeException(
					"Presence Footsteps cannot run because the default custom pack does not exist in the "
						+ new File(util().getModsFolder(), PFHaddon.DEFAULT_PACK_FOLDER_PATH).getAbsolutePath()
						+ " folder.");
		}
		
		reloadBlockMapFromFile();
		reloadAcousticsFromFile();
		this.isolator.setSolver(new PFSolver(this.isolator));
		reloadVariatorFromFile();
		loadSoundsFromPack(this.currentPackFolder);
		
		this.isolator.setGenerator(new PFReaderH(this.isolator));
	}
	
	private void reloadConfig()
	{
		this.config = new ConfigProperty();
		this.config.setProperty("user.volume.0-to-100", 100);
		this.config.setProperty("user.packname.r0", PFHaddon.DEFAULT_PACK_NAME);
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
		
		this.currentPackFolder =
			new File(util().getModsFolder(), (this.config.getAllProperties().containsKey("user.packname.r0")
				? this.config.getString("user.packname.r0") : PFHaddon.DEFAULT_PACK_NAME) + "/");
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
		
		this.debugButton.signalState(util().areKeysDown(29, 42, 33)); // CTRL SHIFT F
		
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
		return new ConfigProperty();
	}
	
	public void printChat(Object... args)
	{
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
	}
	
}
