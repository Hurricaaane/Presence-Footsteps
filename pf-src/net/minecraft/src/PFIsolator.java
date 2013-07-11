package net.minecraft.src;

import eu.ha3.mc.presencefootsteps.engine.interfaces.Library;
import eu.ha3.mc.presencefootsteps.engine.interfaces.SoundPlayer;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.BlockMap;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.DefaultStepPlayer;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Generator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.GeneratorSettable;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Isolator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Solver;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Variator;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.VariatorSettable;

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

public class PFIsolator implements Isolator, VariatorSettable, GeneratorSettable
{
	@SuppressWarnings("unused")
	private PFHaddon mod; // Unused?
	
	private Library acoustics;
	private Solver solver;
	private BlockMap blockMap;
	private SoundPlayer soundPlayer;
	private DefaultStepPlayer defaultStepPlayer;
	
	private Variator VAR;
	
	private Generator generator;
	
	public PFIsolator(PFHaddon mod)
	{
		this.mod = mod;
	}
	
	@Override
	public void onFrame()
	{
		EntityPlayer ply = Minecraft.getMinecraft().thePlayer;
		
		if (ply == null)
			return;
		
		this.generator.generateFootsteps(ply);
		
		// Delayed sounds
		this.acoustics.think();
	}
	
	//
	
	@Override
	public Library getAcoustics()
	{
		return this.acoustics;
	}
	
	@Override
	public Solver getSolver()
	{
		return this.solver;
	}
	
	@Override
	public BlockMap getBlockMap()
	{
		return this.blockMap;
	}
	
	@Override
	public SoundPlayer getSoundPlayer()
	{
		return this.soundPlayer;
	}
	
	@Override
	public DefaultStepPlayer getDefaultStepPlayer()
	{
		return this.defaultStepPlayer;
	}
	
	//
	
	@Override
	public void setAcoustics(Library acoustics)
	{
		this.acoustics = acoustics;
	}
	
	@Override
	public void setSolver(Solver solver)
	{
		this.solver = solver;
	}
	
	@Override
	public void setBlockMap(BlockMap blockMap)
	{
		this.blockMap = blockMap;
	}
	
	@Override
	public void setSoundPlayer(SoundPlayer soundPlayer)
	{
		this.soundPlayer = soundPlayer;
	}
	
	@Override
	public void setDefaultStepPlayer(DefaultStepPlayer defaultStepPlayer)
	{
		this.defaultStepPlayer = defaultStepPlayer;
	}
	
	//
	
	@Override
	public void setVariator(Variator var)
	{
		this.VAR = var;
		fixVariator(this.generator);
	}
	
	//
	
	@Override
	public void setGenerator(Generator generator)
	{
		this.generator = generator;
		fixVariator(this.generator);
	}
	
	/**
	 * Propagate variators.
	 * 
	 * @param possiblyAVariator
	 */
	private void fixVariator(Object possiblyAVariator)
	{
		if (possiblyAVariator == null)
			return;
		
		if (possiblyAVariator instanceof VariatorSettable)
		{
			((VariatorSettable) possiblyAVariator).setVariator(this.VAR);
		}
	}
}
