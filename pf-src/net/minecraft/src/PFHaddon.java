package net.minecraft.src;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import eu.ha3.easy.EdgeModel;
import eu.ha3.easy.EdgeTrigger;
import eu.ha3.mc.convenience.Ha3StaticUtilities;
import eu.ha3.mc.haddon.SupportsFrameEvents;
import eu.ha3.mc.presencefootsteps.interfaces.EventType;
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
	
	private PFGenerator system;
	private PFUpdate update;
	
	private ConfigProperty blockSound;
	private Map<String, String> blockMap;
	
	private EdgeTrigger debugButton;
	private static boolean isDebugEnabled;
	
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
			}
			
			@Override
			public void onFalseEdge()
			{
			}
		});
		
		//fixInstallation();
		loadSounds();
		
		if (isInstalledMLP())
		{
			this.system = new PFReaderMLP(this);
		}
		else
		{
			this.system = new PFReader4P(this);
		}
		
		reloadVariatorFromFile();
		reloadBlockMapFromFile();
		
		manager().hookFrameEvents(true);
		
		this.update = new PFUpdate(this);
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
				
				PFVariator var = new PFVariator();
				var.loadConfig(config);
				
				this.system.setVariator(var);
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
		final int softBlocks[] = { 2, 18, 19, 35, 60, 78, 80, 81, 110, 111 };
		
		this.blockMap = new LinkedHashMap<String, String>();
		this.blockSound = new ConfigProperty();
		this.blockSound.setProperty("0", "default_material");
		this.blockSound.setProperty("0.flak", "NO_FLAK");
		for (int block : softBlocks)
		{
			this.blockSound.setProperty(Integer.toString(block), "soft");
		}
		this.blockSound.setProperty("default_material.step", "pf_sounds.hoofstep");
		this.blockSound.setProperty("soft.step", "pf_sounds.softstep");
		this.blockSound.commit();
		
		// Load configuration from source
		try
		{
			this.blockSound.setSource(new File(util().getMinecraftDir(), "pf_blockmap.cfg").getCanonicalPath());
			this.blockSound.load();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException("Error caused config not to work: " + e.getMessage());
		}
		createBlockMap();
	}
	
	private void createBlockMap()
	{
		Map<String, String> properties = this.blockSound.getAllProperties();
		for (Entry<String, String> entry : properties.entrySet())
		{
			try
			{
				// blockID = Integer.parseInt(entry.getKey());
				this.blockMap.put(entry.getKey(), entry.getValue());
				
			}
			catch (Exception e)
			{
				log("Error when registering block " + entry.getKey() + ": " + e.getMessage());
			}
			
		}
		
	}
	
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
	
	@Override
	public void onFrame(float semi)
	{
		EntityPlayer ply = manager().getMinecraft().thePlayer;
		
		if (ply == null)
			return;
		
		this.system.generateFootsteps(ply);
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
	
	public String getSoundForBlock(int block, int meta, EventType event)
	{
		String material = null;
		
		if (this.blockMap.containsKey(block + "_" + meta))
		{
			material = this.blockMap.get(block + "_" + meta);
		}
		else if (this.blockMap.containsKey(Integer.toString(block)))
		{
			material = this.blockMap.get(Integer.toString(block));
		}
		else
		{
			material = this.blockMap.get("0");
		}
		
		return getSoundForMaterial(material, event);
	}
	
	public String getFlakForBlock(int block, int meta, EventType event)
	{
		String material = null;
		
		if (this.blockMap.containsKey(block + "_" + meta + ".flak"))
		{
			material = this.blockMap.get(block + "_" + meta + ".flak");
		}
		else if (this.blockMap.containsKey(Integer.toString(block) + ".flak"))
		{
			material = this.blockMap.get(Integer.toString(block) + ".flak");
		}
		else
			return null;
		
		return getSoundForMaterial(material, event);
	}
	
	public String getSoundForMaterial(String material, EventType event)
	{
		if (material == null || material.equals("FALLBACK"))
			return null;
		
		if (material.equals("BLANK") || material.equals("NOT_EMITTER"))
			return material;
		
		if (event == EventType.WALK)
			return this.blockMap.get(material + ".step");
		else if (event == EventType.JUMP)
			return this.blockMap.containsKey(material + ".jump")
				? this.blockMap.get(material + ".jump") : getSoundForMaterial(material, EventType.WALK);
		else
			//if (event == PFEventType.LAND)
			return this.blockMap.containsKey(material + ".land")
				? this.blockMap.get(material + ".land") : getSoundForMaterial(material, EventType.WALK);
		
	}
	
}
