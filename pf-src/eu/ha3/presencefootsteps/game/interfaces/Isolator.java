package eu.ha3.presencefootsteps.game.interfaces;

import eu.ha3.presencefootsteps.engine.interfaces.Library;
import eu.ha3.presencefootsteps.engine.interfaces.SoundPlayer;

import net.minecraft.entity.player.EntityPlayer;

/* x-placeholder-wtfplv2 */

public interface Isolator {
	
	public void reload();
	
	public void onFrame(EntityPlayer ply);
	
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