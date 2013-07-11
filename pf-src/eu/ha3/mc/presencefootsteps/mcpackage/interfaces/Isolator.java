package eu.ha3.mc.presencefootsteps.mcpackage.interfaces;

import eu.ha3.mc.presencefootsteps.mcpackage.implem.AcousticsManager;

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

public interface Isolator
{
	
	public abstract void onFrame();
	
	public abstract AcousticsManager getAcoustics();
	
	public abstract Solver getSolver();
	
	public abstract BlockMap getBlockMap();
	
	public abstract void setAcoustics(AcousticsManager acoustics);
	
	public abstract void setSolver(Solver solver);
	
	public abstract void setBlockMap(BlockMap blockMap);
	
}