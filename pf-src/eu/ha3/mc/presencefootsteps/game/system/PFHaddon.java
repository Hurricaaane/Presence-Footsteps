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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import eu.ha3.easy.EdgeModel;
import eu.ha3.easy.EdgeTrigger;
import eu.ha3.mc.convenience.Ha3StaticUtilities;
import eu.ha3.mc.haddon.Identity;
import eu.ha3.mc.haddon.OperatorCaster;
import eu.ha3.mc.haddon.implem.HaddonIdentity;
import eu.ha3.mc.haddon.implem.HaddonImpl;
import eu.ha3.mc.haddon.supporting.SupportsFrameEvents;
import eu.ha3.mc.presencefootsteps.game.user.PFGuiMenu;
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
import eu.ha3.mc.quick.update.NotifiableHaddon;
import eu.ha3.mc.quick.update.UpdateNotifier;
import eu.ha3.util.property.simple.ConfigProperty;
import eu.ha3.util.property.simple.InputStreamConfigProperty;

/* x-placeholder-wtfplv2 */

public class PFHaddon extends HaddonImpl
	implements SupportsFrameEvents, IResourceManagerReloadListener, NotifiableHaddon
{
	protected final String NAME = "Presence Footsteps";
	protected final int VERSION = 1;
	protected final String FOR = "1.6.2";
	protected final String ADDRESS = "http://presencefootsteps.ha3.eu";
	protected final Identity identity = new HaddonIdentity(this.NAME, this.VERSION, this.FOR, this.ADDRESS);
	
	private final Chatter chatter = new Chatter(this, this.NAME);
	
	private File presenceDir;
	private File packsFolder;
	
	private EdgeTrigger debugButton;
	private static boolean isDebugEnabled;
	
	private ConfigProperty config;
	
	private PFIsolator isolator;
	
	private UpdateNotifier updateNotifier;
	
	private static String DEFAULT_PACK_NAME = "pf_presence";
	
	private PFResourcePackDealer dealer;
	private boolean firstTickPassed;
	private boolean mlpDetectedFirst;
	
	private long pressedOptionsTime;
	
	private boolean hasResourcePacks;
	private boolean hasResourcePacks_FixMe;
	
	@Override
	public void onLoad()
	{
		this.updateNotifier = new UpdateNotifier(this, "http://q.mc.ha3.eu/query/pf-main-version.xml?ver=%d");
		
		util().registerPrivateSetter("Entity_nextStepDistance", Entity.class, -1, "nextStepDistance", "c");
		
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
		
		this.dealer = new PFResourcePackDealer();
		
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
		
		reloadEverything(false);
		
		IResourceManager resMan = Minecraft.getMinecraft().getResourceManager();
		if (resMan instanceof IReloadableResourceManager)
		{
			((IReloadableResourceManager) resMan).registerReloadListener(this);
		}
		
		((OperatorCaster) op()).setFrameEnabled(true);
	}
	
	public void reloadEverything(boolean nested)
	{
		this.isolator = new PFIsolator(this);
		
		// Sets up the pack
		reloadConfig();
		
		List<ResourcePackRepository.Entry> repo = this.dealer.findResourcePacks();
		if (repo.size() == 0)
		{
			PFHaddon.log("Presence Footsteps didn't find any compatible resource pack.");
			this.hasResourcePacks = false;
			
			this.isolator.setGenerator(null);
			
			return;
		}
		this.hasResourcePacks = true;
		
		reloadBlockMap(repo);
		reloadPrimitiveMap(repo);
		reloadAcoustics(repo);
		this.isolator.setSolver(new PFSolver(this.isolator));
		reloadVariator(repo);
		
		this.isolator.setGenerator(!getConfig().getBoolean("mlp.enabled")
			? new PFReaderH(this.isolator) : new PFReaderQP(this.isolator));
	}
	
	private void reloadConfig()
	{
		this.config = new ConfigProperty();
		this.updateNotifier.fillDefaults(this.config);
		this.config.setProperty("user.volume.0-to-100", 70);
		this.config.setProperty("user.packname.r0", PFHaddon.DEFAULT_PACK_NAME);
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
		
		this.updateNotifier.loadConfig(this.config);
		
		//this.currentPackFolder =
		//	new File(this.packsFolder, (this.config.getAllProperties().containsKey("user.packname.r0")
		//		? this.config.getString("user.packname.r0") : PFHaddon.DEFAULT_PACK_NAME)
		//		+ "/");
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
				PFHaddon.debug("No variator found in " + pack.getResourcePackName() + ": " + e.getMessage());
			}
		}
		if (working == 0)
		{
			PFHaddon.log("No variators found in " + repo.size() + " packs!");
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
				PFHaddon.debug("No blockmap found in " + pack.getResourcePackName() + ": " + e.getMessage());
			}
		}
		if (working == 0)
		{
			PFHaddon.log("No blockmaps found in " + repo.size() + " packs!");
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
				PFHaddon.debug("No primitivemap found in " + pack.getResourcePackName() + ": " + e.getMessage());
			}
		}
		if (working == 0)
		{
			PFHaddon.log("No blockmaps found in " + repo.size() + " packs!");
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
				PFHaddon.debug("No acoustics found in " + pack.getResourcePackName() + ": " + e.getMessage());
			}
		}
		if (working == 0)
		{
			PFHaddon.log("No blockmaps found in " + repo.size() + " packs!");
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
			if (util().isCurrentScreen(null))
			{
				Minecraft.getMinecraft().displayGuiScreen(new PFGuiMenu((GuiScreen) util().getCurrentScreen(), this));
				setDebugEnabled(false);
			}
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
				this.chatter.printChat(ChatColorsSimple.COLOR_RED, "Resource Pack not loaded!");
				this.chatter.printChatShort(ChatColorsSimple.COLOR_WHITE, "You need to activate "
					+ "\"Presence Footsteps Resource Pack\" in the Minecraft Options menu for it to run.");
			}
		}
		if (this.hasResourcePacks_FixMe && this.hasResourcePacks)
		{
			this.hasResourcePacks_FixMe = false;
			this.chatter.printChat(ChatColorsSimple.COLOR_BRIGHTGREEN, "It should work now!");
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
	
	@Override
	public ConfigProperty getConfig()
	{
		return this.config;
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
	
	@Override
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
	public void onResourceManagerReload(IResourceManager var1)
	{
		PFHaddon.log("Resource Pack reload detected...");
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
	
}
