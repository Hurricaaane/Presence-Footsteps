package eu.ha3.presencefootsteps.game.implem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import eu.ha3.presencefootsteps.engine.implem.AcousticsLibrary;
import eu.ha3.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.presencefootsteps.engine.interfaces.Options;
import eu.ha3.presencefootsteps.engine.interfaces.SoundPlayer;
import eu.ha3.presencefootsteps.game.Association;
import eu.ha3.presencefootsteps.game.interfaces.DefaultStepPlayer;
import eu.ha3.presencefootsteps.game.interfaces.Isolator;
import eu.ha3.presencefootsteps.log.PFLog;

/**
 * A Library that can also play sounds and default footsteps.
 * 
 * @author Hurry
 */
public class AcousticsManager extends AcousticsLibrary implements SoundPlayer, DefaultStepPlayer {
	private Isolator isolator;
	
	private final Random random = new Random();
	private List<PendingSound> pending = new ArrayList<PendingSound>();
	private long minimum;
	
	private boolean USING_LATENESS = true;
	private boolean USING_EARLYNESS = true;
	private float LATENESS_THRESHOLD_DIVIDER = 1.5f;
	private double EARLYNESS_THRESHOLD_POW = 0.75d;
	
	public AcousticsManager(Isolator isolator) {
		this.isolator = isolator;
	}
	
	@Override
	public void playStep(EntityLivingBase entity, Association assos) {
		Block block = assos.getBlock();
		if (!block.getMaterial().isLiquid() && block.stepSound != null) {
			Block.SoundType soundType = block.stepSound;
			if (entity.worldObj.getBlockState(assos.pos(0, 1, 0)).getBlock() == Blocks.snow_layer) {
				soundType = Blocks.snow_layer.stepSound;
			}
			
			//entity.worldObj.playSoundAtEntity(entity, soundType.getStepSound(), soundType.getVolume() * 0.15F, soundType.getFrequency());
			actuallyPlaySound(entity, soundType.getStepSound(), soundType.getVolume() * 0.15F, soundType.getFrequency());
		}
	}
	
	@Override
	public void playSound(Object location, String soundName, float volume, float pitch, Options options) {
		if (!(location instanceof Entity)) return;
		if (options != null) {
			if (options.hasOption("delay_min") && options.hasOption("delay_max")) {
				long delay = randAB(this.random, (Long) options.getOption("delay_min"), (Long) options.getOption("delay_max"));
				
				if (delay < minimum) {
					minimum = delay;
				}
				
				pending.add(new PendingSound(location, soundName, volume, pitch, null, System.currentTimeMillis() + delay, options.hasOption("skippable") ? -1 : (Long) options.getOption("delay_max")));
				return;
			}
		}
		PFLog.debug("    Playing sound " + soundName + " (" + String.format(Locale.ENGLISH, "v%.2f, p%.2f", volume, pitch) + ")");
		actuallyPlaySound((Entity) location, soundName, volume, pitch);
	}
	
	protected void actuallyPlaySound(Entity location, String soundName, float volume, float pitch) {
		//location.worldObj.playSoundAtEntity(location, soundName, volume, pitch);
		//location.playSound(soundName, volume, pitch);
		Minecraft mc = Minecraft.getMinecraft();
		double d0 = mc.getRenderViewEntity().getDistanceSq(location.posX, location.posY, location.posZ);
        PositionedSoundRecord positionedsoundrecord = new PositionedSoundRecord(new ResourceLocation(soundName), volume, pitch, (float)location.posX, (float)location.posY, (float)location.posZ);

        if (d0 > 100.0D) {
            double d1 = Math.sqrt(d0) / 40.0D;
            mc.getSoundHandler().playDelayedSound(positionedsoundrecord, (int)(d1 * 20.0D));
        } else {
            mc.getSoundHandler().playSound(positionedsoundrecord);
        }
	}
	
	private long randAB(Random rng, long a, long b) {
		return a >= b ? a : a + rng.nextInt((int) b + 1);
	}
	
	@Override
	public Random getRNG() {
		return random;
	}
	
	@Override
	protected void onAcousticNotFound(Object location, String acousticName, EventType event, Options inputOptions) {
		PFLog.log("Tried to play a missing acoustic: " + acousticName);
	}
	
	@Override
	public void think() {
		if (pending.isEmpty() || System.currentTimeMillis() < minimum) return;
		
		long newMinimum = Long.MAX_VALUE;
		long time = System.currentTimeMillis();
		
		Iterator<PendingSound> iter = pending.iterator();
		while (iter.hasNext()) {
			PendingSound sound = iter.next();
			
			if (time >= sound.getTimeToPlay() || USING_EARLYNESS && time >= sound.getTimeToPlay() - Math.pow(sound.getMaximumBase(), EARLYNESS_THRESHOLD_POW)) {
				if (USING_EARLYNESS && time < sound.getTimeToPlay()) {
					PFLog.debug("    Playing early sound (early by " + (sound.getTimeToPlay() - time) + "ms, tolerence is " + Math.pow(sound.getMaximumBase(), EARLYNESS_THRESHOLD_POW));
				}
				
				long lateness = time - sound.getTimeToPlay();
				if (!USING_LATENESS || sound.getMaximumBase() < 0 || lateness <= sound.getMaximumBase() / LATENESS_THRESHOLD_DIVIDER) {
					sound.playSound(this);
				} else {
					PFLog.debug("    Skipped late sound (late by " + lateness + "ms, tolerence is " + sound.getMaximumBase() / LATENESS_THRESHOLD_DIVIDER + "ms)");
				}
				iter.remove();
			} else {
				newMinimum = sound.getTimeToPlay();
			}
		}
		
		minimum = newMinimum;
	}
	
	@Override
	protected SoundPlayer mySoundPlayer() {
		return isolator.getSoundPlayer();
	}
}
