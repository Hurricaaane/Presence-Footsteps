package eu.ha3.mc.presencefootsteps.mcpackage.interfaces;

import eu.ha3.mc.presencefootsteps.engine.interfaces.Library;
import eu.ha3.mc.presencefootsteps.engine.interfaces.SoundPlayer;

/* x-placeholder-wtfplv2 */

public interface Isolator {
	
	public void onFrame();
	
	public Library getAcoustics();
	
	public Solver getSolver();
	
	public BlockMap getBlockMap();
	
	public PrimitiveMap getPrimitiveMap();
	
	public SoundPlayer getSoundPlayer();
	
	public DefaultStepPlayer getDefaultStepPlayer();
	
	public void setAcoustics(Library acoustics);
	
	public void setSolver(Solver solver);
	
	public void setBlockMap(BlockMap blockMap);
	
	public void setPrimitiveMap(PrimitiveMap primitiveMap);
	
	public void setSoundPlayer(SoundPlayer soundPlayer);
	
	public void setDefaultStepPlayer(DefaultStepPlayer defaultStepPlayer);
	
}