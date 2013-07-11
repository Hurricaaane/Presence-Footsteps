package net.minecraft.src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import eu.ha3.easy.EdgeModel;
import eu.ha3.easy.EdgeTrigger;
import eu.ha3.mc.convenience.Ha3StaticUtilities;
import eu.ha3.mc.haddon.SupportsFrameEvents;
import eu.ha3.mc.presencefootsteps.mcpackage.implem.AcousticsManager;
import eu.ha3.mc.presencefootsteps.mcpackage.implem.NormalVariator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Variator;
import eu.ha3.mc.presencefootsteps.mod.UpdateNotifier;
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
	
	private UpdateNotifier update;
	
	private EdgeTrigger debugButton;
	private static boolean isDebugEnabled;
	
	private PFIsolator isolator;
	
	@Override
	public void onLoad()
	{
		this.debugButton = new EdgeTrigger(new EdgeModel() {
			@Override
			public void onTrueEdge()
			{
				setDebugEnabled(true);
				reloadVariatorFromFile();
				reloadBlockMapFromFile();
				reloadAcousticsFromFile();
			}
			
			@Override
			public void onFalseEdge()
			{
			}
		});
		
		loadSounds();
		
		/*if (isInstalledMLP())
		{
			this.generator = new PFReaderMLP(this.isolator);
		}
		else
		{
			this.generator = new PFReader4P(this.isolator);
		}*/
		
		this.isolator = new PFIsolator(this);
		
		reloadBlockMapFromFile();
		reloadAcousticsFromFile();
		this.isolator.setSolver(new PFSolver(this.isolator));
		reloadVariatorFromFile();
		
		this.isolator.setGenerator(new PFReaderH(this.isolator));
		
		manager().hookFrameEvents(true);
		
		this.update = new UpdateNotifier(this);
		this.update.attempt();
		
	}
	
	private void reloadVariatorFromFile()
	{
		File configFile = new File(util().getMinecraftDir(), "pf.cfg");
		if (configFile.exists())
		{
			log("Config file found. Loading...");
			try
			{
				ConfigProperty config = new ConfigProperty();
				config.setSource(configFile.getCanonicalPath());
				config.load();
				
				Variator var = new NormalVariator();
				var.loadConfig(config);
				
				this.isolator.setVariator(var);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			log("Loaded.");
			
		}
	}
	
	private void reloadBlockMapFromFile()
	{
		PFBlockMap blockMap = new PFBlockMap();
		
		// Load configuration from source
		try
		{
			ConfigProperty blockSound = new ConfigProperty();
			blockSound.setSource(new File(util().getMinecraftDir(), "pf_blockmap.cfg").getCanonicalPath());
			blockSound.load();
			
			new PropertyBlockMap_Engine0().setup(blockSound, blockMap);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException("Error caused config not to work: " + e.getMessage());
		}
		
		this.isolator.setBlockMap(blockMap);
	}
	
	private void reloadAcousticsFromFile()
	{
		AcousticsManager acoustics = new AcousticsManager();
		String jasonString;
		try
		{
			jasonString =
				new Scanner(new File(util().getMinecraftDir(), "presencefootsteps/presence_acoustics.json"))
					.useDelimiter("\\Z").next();
			
			new JasonAcoustics_Engine0("pf_library.presence.").parseJSON(jasonString, acoustics);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
		this.isolator.setAcoustics(acoustics);
	}
	
	//
	
	private boolean isInstalledMLP()
	{
		return Ha3StaticUtilities.classExists("Pony", this)
			|| Ha3StaticUtilities.classExists("net.minecraft.src.Pony", this);
	}
	
	private void loadSounds()
	{
		File dir = new File(util().getMinecraftDir(), "assets/minecraft/sound/pf_library/");
		if (dir.exists())
		{
			loadResource(dir, "pf_library/");
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
	private void loadResource(File par1File, String par2Str)
	{
		File[] filesInThisDir = par1File.listFiles();
		int fileCount = filesInThisDir.length;
		
		for (int i = 0; i < fileCount; ++i)
		{
			File file = filesInThisDir[i];
			
			if (file.isDirectory())
			{
				loadResource(file, par2Str + file.getName() + "/");
			}
			else
			{
				try
				{
					getManager().getMinecraft().sndManager.addSound(par2Str + file.getName());
				}
				catch (Exception var9)
				{
					log("Failed to add " + par2Str + file.getName());
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
