package net.minecraft.src;

import java.util.Random;

import eu.ha3.mc.presencefootsteps.implem.AcousticsLibrary;
import eu.ha3.mc.presencefootsteps.interfaces.Options;
import eu.ha3.mc.presencefootsteps.interfaces.SoundPlayer;

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

public class PFAcousticsManager extends AcousticsLibrary implements SoundPlayer, PFDefaultStepPlayer
{
	private final Random random;
	
	public PFAcousticsManager()
	{
		this.random = new Random();
	}
	
	@Override
	public void playStep(EntityLivingBase entity, int xx, int yy, int zz, int blockID)
	{
		entity.playStepSound(xx, yy, zz, blockID);
	}
	
	@Override
	public void playSound(Object location, String soundName, float volume, float pitch, Options options)
	{
		if (!(location instanceof Entity))
			return;
		
		((Entity) location).playSound(soundName, volume, pitch);
	}
	
	@Override
	public Random getRNG()
	{
		return this.random;
	}
	
	@Override
	protected void onMaterialNotFound()
	{
		throw new RuntimeException("Tried to play a missing acoustics. Check if the material exists before playing it.");
	}
	
}
