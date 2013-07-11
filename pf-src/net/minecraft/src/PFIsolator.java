package net.minecraft.src;

import eu.ha3.mc.presencefootsteps.mcpackage.implem.AcousticsManager;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.BlockMap;
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
	
	private AcousticsManager acoustics;
	private Solver solver;
	private BlockMap blockMap;
	
	private Variator VAR;
	
	private Generator genetaror;
	
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
		
		this.genetaror.generateFootsteps(ply);
		
		// Delayed sounds
		this.acoustics.generateFootsteps(null);
	}
	
	//
	
	@Override
	public AcousticsManager getAcoustics()
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
	
	//
	
	@Override
	public void setAcoustics(AcousticsManager acoustics)
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
	
	//
	
	@Override
	public void setVariator(Variator var)
	{
		this.VAR = var;
		fixVariator(this.genetaror);
	}
	
	//
	
	@Override
	public void setGenerator(Generator generator)
	{
		this.genetaror = generator;
		fixVariator(this.genetaror);
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
