package eu.ha3.mc.presencefootsteps.mcpackage.implem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityLivingBase;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.PFAccessors;
import net.minecraft.src.PFHaddon;
import eu.ha3.mc.presencefootsteps.engine.implem.AcousticsLibrary;
import eu.ha3.mc.presencefootsteps.engine.interfaces.Options;
import eu.ha3.mc.presencefootsteps.engine.interfaces.SoundPlayer;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.DefaultStepPlayer;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Generator;

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

/**
 * A Library that can also play sounds and default footsteps.
 * 
 * @author Hurry
 * 
 */
public class AcousticsManager extends AcousticsLibrary implements SoundPlayer, DefaultStepPlayer, Generator
{
	private final Random random;
	private List<PendingSound> pending;
	private long minimum;
	
	public AcousticsManager()
	{
		this.random = new Random();
		this.pending = new ArrayList<PendingSound>();
		
		this.myPlayer = this;
	}
	
	@Override
	public void playStep(EntityLivingBase entity, int xx, int yy, int zz, int blockID)
	{
		PFAccessors.getInstance().playStep(entity, xx, yy, zz, blockID);
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
					+ delay));
			}
			else
			{
				((Entity) location).playSound(soundName, volume, pitch);
			}
		}
		else
		{
			((Entity) location).playSound(soundName, volume, pitch);
		}
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
	protected void onMaterialNotFound()
	{
		PFHaddon.log("Tried to play a missing acoustic.");
	}
	
	@Override
	public void generateFootsteps(EntityPlayer ply)
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
			if (time > sound.getTimeToPlay())
			{
				sound.playSound(this);
				iter.remove();
			}
			else
			{
				newMinimum = sound.getTimeToPlay();
			}
		}
		
		this.minimum = newMinimum;
	}
}
