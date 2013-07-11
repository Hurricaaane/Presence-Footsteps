package eu.ha3.mc.presencefootsteps.mcpackage.interfaces;

import eu.ha3.mc.presencefootsteps.engine.interfaces.Library;
import eu.ha3.mc.presencefootsteps.engine.interfaces.SoundPlayer;

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
	
	public abstract Library getAcoustics();
	
	public abstract Solver getSolver();
	
	public abstract BlockMap getBlockMap();
	
	public abstract SoundPlayer getSoundPlayer();
	
	public abstract DefaultStepPlayer getDefaultStepPlayer();
	
	//
	
	public abstract void setAcoustics(Library acoustics);
	
	public abstract void setSolver(Solver solver);
	
	public abstract void setBlockMap(BlockMap blockMap);
	
	public abstract void setSoundPlayer(SoundPlayer soundPlayer);
	
	public abstract void setDefaultStepPlayer(DefaultStepPlayer defaultStepPlayer);
	
}