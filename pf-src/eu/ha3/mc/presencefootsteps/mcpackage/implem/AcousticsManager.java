package eu.ha3.mc.presencefootsteps.mcpackage.implem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import eu.ha3.mc.presencefootsteps.engine.implem.AcousticsLibrary;
import eu.ha3.mc.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.mc.presencefootsteps.engine.interfaces.Options;
import eu.ha3.mc.presencefootsteps.engine.interfaces.SoundPlayer;
import eu.ha3.mc.presencefootsteps.log.PFLog;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.DefaultStepPlayer;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Isolator;

/* x-placeholder-wtfplv2 */

/**
 * A Library that can also play sounds and default footsteps.
 * 
 * @author Hurry
 */
public class AcousticsManager extends AcousticsLibrary implements SoundPlayer, DefaultStepPlayer
{
	private Isolator isolator;
	
	private final Random random;
	private List<PendingSound> pending;
	private long minimum;
	
	private boolean USING_LATENESS = true;
	private boolean USING_EARLYNESS = true;
	private float LATENESS_THRESHOLD_DIVIDER = 1.5f;
	private double EARLYNESS_THRESHOLD_POW = 0.75d;
	
	public AcousticsManager(Isolator isolator)
	{
		this.isolator = isolator;
		
		this.random = new Random();
		this.pending = new ArrayList<PendingSound>();
	}
	
	@Override
	public void playStep(EntityLivingBase entity, int xx, int yy, int zz, Block blockID)
	{
		//playStepSound
		//entity.func_145780_a(xx, yy, zz, blockID);
		
		if (blockId == null) {
			return;
		}
		
		Block.SoundType soundType = blockID.stepSound;
		
		if (soundType == null) {
			return;
		}
		
		if (Minecraft.getMinecraft().theWorld.getBlock(xx, yy + 1, zz) == Blocks.snow_layer)
		{
			soundType = Blocks.snow_layer.stepSound;
			entity.playSound(soundType.func_150498_e(), soundType.func_150497_c() * 0.15F, soundType.func_150494_d());
		}
		else if (!blockID.getMaterial().isLiquid())
		{
			entity.playSound(soundType.func_150498_e(), soundType.func_150497_c() * 0.15F, soundType.func_150494_d());
		}
	}
	
	@Override
	public void playSound(Object location, String soundName, float volume, float pitch, Options options)
	{
		if (!(location instanceof Entity))
			return;
		
		if (options != null)
		{
			if (options.hasOption("delay_min") && options.hasOption("delay_max"))
			{
				long delay =
					randAB(this.random, (Long) options.getOption("delay_min"), (Long) options.getOption("delay_max"));
				
				if (delay < this.minimum)
				{
					this.minimum = delay;
				}
				
				this.pending.add(new PendingSound(location, soundName, volume, pitch, null, System.currentTimeMillis()
					+ delay, options.hasOption("skippable") ? -1 : (Long) options.getOption("delay_max")));
			}
			else
			{
				actuallyPlaySound((Entity) location, soundName, volume, pitch);
			}
		}
		else
		{
			actuallyPlaySound((Entity) location, soundName, volume, pitch);
		}
	}
	
	protected void actuallyPlaySound(Entity location, String soundName, float volume, float pitch)
	{
		PFLog.debug("    Playing sound "
			+ soundName + " (" + String.format(Locale.ENGLISH, "v%.2f, p%.2f", volume, pitch) + ")");
		location.playSound(soundName, volume, pitch);
	}
	
	private long randAB(Random rng, long a, long b)
	{
		if (a >= b)
			return a;
		
		return a + rng.nextInt((int) b + 1);
	}
	
	//
	
	@Override
	public Random getRNG()
	{
		return this.random;
	}
	
	@Override
	protected void onAcousticNotFound(Object location, String acousticName, EventType event, Options inputOptions)
	{
		PFLog.log("Tried to play a missing acoustic: " + acousticName);
	}
	
	@Override
	public void think()
	{
		if (this.pending.isEmpty())
			return;
		
		if (System.currentTimeMillis() < this.minimum)
			return;
		
		long newMinimum = Long.MAX_VALUE;
		long time = System.currentTimeMillis();
		
		for (Iterator<PendingSound> iter = this.pending.iterator(); iter.hasNext();)
		{
			PendingSound sound = iter.next();
			
			if (time >= sound.getTimeToPlay()
				|| this.USING_EARLYNESS
				&& time >= sound.getTimeToPlay() - Math.pow(sound.getMaximumBase(), this.EARLYNESS_THRESHOLD_POW))
			{
				if (this.USING_EARLYNESS && time < sound.getTimeToPlay())
				{
					PFLog.debug("    Playing early sound (early by "
						+ (sound.getTimeToPlay() - time) + "ms, tolerence is "
						+ Math.pow(sound.getMaximumBase(), this.EARLYNESS_THRESHOLD_POW));
				}
				
				long lateness = time - sound.getTimeToPlay();
				if (!this.USING_LATENESS
					|| sound.getMaximumBase() < 0
					|| lateness <= sound.getMaximumBase() / this.LATENESS_THRESHOLD_DIVIDER)
				{
					sound.playSound(this);
				}
				else
				{
					PFLog.debug("    Skipped late sound (late by "
						+ lateness + "ms, tolerence is " + sound.getMaximumBase() / this.LATENESS_THRESHOLD_DIVIDER
						+ "ms)");
				}
				iter.remove();
			}
			else
			{
				newMinimum = sound.getTimeToPlay();
			}
		}
		
		this.minimum = newMinimum;
	}
	
	@Override
	protected SoundPlayer mySoundPlayer()
	{
		return this.isolator.getSoundPlayer();
	}
}
