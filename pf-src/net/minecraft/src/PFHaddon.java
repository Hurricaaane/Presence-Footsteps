package net.minecraft.src;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import eu.ha3.easy.EdgeModel;
import eu.ha3.easy.EdgeTrigger;
import eu.ha3.mc.convenience.Ha3StaticUtilities;
import eu.ha3.mc.haddon.SupportsFrameEvents;
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
	
	private PFReader system;
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
				reloadBlockMapFromFile();
			}
			
			@Override
			public void onFalseEdge()
			{
			}
		});
		
		fixInstallation();
		loadSounds();
		
		if (isInstalledMLP())
		{
			this.system = new PFReaderMLP(this);
		}
		else
		{
			this.system = new PFReader4P(this);
		}
		
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
		
		reloadBlockMapFromFile();
		
		manager().hookFrameEvents(true);
		
		this.update = new PFUpdate(this);
		this.update.attempt();
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
	
	private void fixInstallation()
	{
		File folder = new File(util().getMinecraftDir(), "resources/sound3/pf_sounds");
		if (!folder.exists())
		{
			log("Did not find folder resources/sound3/pf_sounds/. Attempting first installation");
			folder.mkdirs();
		}
		
		String[] names = { "dash1.wav", "hoofstep1.wav", "softstep1.wav", "land1.wav", "wing1.wav" };
		
		for (String name : names)
		{
			InputStream stream = null;
			try
			{
				File file = new File(folder, name);
				
				if (!file.exists())
				{
					URL toInstall =
						net.minecraft.src.Minecraft.class.getResource("/resources/sound/pf_sounds/" + name);
					stream = toInstall.openStream();
					if (stream != null)
					{
						isToFile(stream, file);
					}
					
					/*File inJarFile = new File(toInstall.getFile());
					
					if (inJarFile.exists())
					{
						log("Did not find file " + name + ". Installing...");
						copyFile(new File(toInstall.getFile()), file);
					}*/
					
				}
			}
			catch (Exception e)
			{
				PFHaddon.log("Could not fix " + name + ": " + e.getMessage());
			}
			finally
			{
				try
				{
					if (stream != null)
					{
						stream.close();
					}
				}
				catch (Exception e)
				{
				}
			}
			
		}
	}
	
	// from
	// http://stackoverflow.com/questions/106770/standard-concise-way-to-copy-a-file-in-java
	private static void isToFile(InputStream sourceStream, File destFile) throws IOException
	{
		if (!destFile.exists())
		{
			destFile.createNewFile();
		}
		
		FileOutputStream fos = null;
		
		try
		{
			fos = new FileOutputStream(destFile);
			
			byte buffer[] = new byte[1024];
			int length;
			while ((length = sourceStream.read(buffer)) > 0)
			{
				fos.write(buffer, 0, length);
			}
		}
		finally
		{
			if (sourceStream != null)
			{
				sourceStream.close();
			}
			if (fos != null)
			{
				fos.close();
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
		File dir = new File(util().getMinecraftDir(), "resources/sound3/pf_sounds/");
		if (dir.exists())
		{
			loadResource(dir, "sound3/pf_sounds/");
		}
	}
	
	@Override
	public void onFrame(float semi)
	{
		EntityPlayer ply = manager().getMinecraft().thePlayer;
		
		if (ply == null)
			return;
		
		this.system.frame(ply);
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
					String fileRep = par2Str + file.getName();
					fileRep = fileRep.substring(fileRep.indexOf("/") + 1);
					fileRep = fileRep.substring(0, fileRep.indexOf("."));
					while (Character.isDigit(fileRep.charAt(fileRep.length() - 1)))
					{
						fileRep = fileRep.substring(0, fileRep.length() - 1);
					}
					fileRep = fileRep.replaceAll("/", ".");
					
					getManager().getMinecraft().installResource(par2Str + file.getName(), file);
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
	
	public String getSoundForBlock(int block, int meta, PFEventType event)
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
	
	public String getFlakForBlock(int block, int meta, PFEventType event)
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
	
	public String getSoundForMaterial(String material, PFEventType event)
	{
		if (material == null || material.equals("FALLBACK"))
			return null;
		
		if (material.equals("BLANK") || material.equals("NOT_EMITTER"))
			return material;
		
		if (event == PFEventType.STEP)
			return this.blockMap.get(material + ".step");
		else if (event == PFEventType.JUMP)
			return this.blockMap.containsKey(material + ".jump")
				? this.blockMap.get(material + ".jump") : getSoundForMaterial(material, PFEventType.STEP);
		else
			//if (event == PFEventType.LAND)
			return this.blockMap.containsKey(material + ".land")
				? this.blockMap.get(material + ".land") : getSoundForMaterial(material, PFEventType.STEP);
		
	}
	
}
