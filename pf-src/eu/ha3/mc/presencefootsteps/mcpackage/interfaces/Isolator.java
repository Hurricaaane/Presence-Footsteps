package eu.ha3.mc.presencefootsteps.mcpackage.interfaces;

import eu.ha3.mc.presencefootsteps.engine.interfaces.Library;
import eu.ha3.mc.presencefootsteps.engine.interfaces.SoundPlayer;

/* x-placeholder-wtfplv2 */

public interface Isolator
{
	
	public abstract void onFrame();
	
	public abstract Library getAcoustics();
	
	public abstract Solver getSolver();
	
	public abstract BlockMap getBlockMap();
	
	public abstract PrimitiveMap getPrimitiveMap();
	
	public abstract SoundPlayer getSoundPlayer();
	
	public abstract DefaultStepPlayer getDefaultStepPlayer();
	
	//
	
	public abstract void setAcoustics(Library acoustics);
	
	public abstract void setSolver(Solver solver);
	
	public abstract void setBlockMap(BlockMap blockMap);
	
	public abstract void setPrimitiveMap(PrimitiveMap primitiveMap);
	
	public abstract void setSoundPlayer(SoundPlayer soundPlayer);
	
	public abstract void setDefaultStepPlayer(DefaultStepPlayer defaultStepPlayer);
	
}