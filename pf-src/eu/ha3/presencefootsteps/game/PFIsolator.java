package eu.ha3.presencefootsteps.game;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import eu.ha3.presencefootsteps.engine.interfaces.Library;
import eu.ha3.presencefootsteps.engine.interfaces.SoundPlayer;
import eu.ha3.presencefootsteps.game.interfaces.BlockMap;
import eu.ha3.presencefootsteps.game.interfaces.DefaultStepPlayer;
import eu.ha3.presencefootsteps.game.interfaces.Generator;
import eu.ha3.presencefootsteps.game.interfaces.GeneratorSettable;
import eu.ha3.presencefootsteps.game.interfaces.Isolator;
import eu.ha3.presencefootsteps.game.interfaces.PrimitiveMap;
import eu.ha3.presencefootsteps.game.interfaces.Solver;
import eu.ha3.presencefootsteps.game.interfaces.Variator;
import eu.ha3.presencefootsteps.game.interfaces.VariatorSettable;
import eu.ha3.presencefootsteps.main.PFHaddon;

public class PFIsolator implements Isolator, VariatorSettable, GeneratorSettable {
	
	private Library acoustics;
	private Solver solver;
	private BlockMap blockMap;
	private PrimitiveMap primitiveMap;
	private SoundPlayer soundPlayer;
	private DefaultStepPlayer defaultStepPlayer;
	
	private Variator VAR;
	
	private Generator generator;
	
	public PFIsolator(PFHaddon mod) {
		
	}
	
	@Override
	public void onFrame() {
		if (generator == null) return;
		
		EntityPlayer ply = Minecraft.getMinecraft().thePlayer;
		
		if (ply == null) return;
		
		generator.generateFootsteps(ply);
		acoustics.think(); // Delayed sounds
	}
	
	@Override
	public Library getAcoustics() {
		return acoustics;
	}
	
	@Override
	public Solver getSolver() {
		return solver;
	}
	
	@Override
	public BlockMap getBlockMap() {
		return blockMap;
	}
	
	@Override
	public PrimitiveMap getPrimitiveMap() {
		return primitiveMap;
	}
	
	@Override
	public SoundPlayer getSoundPlayer() {
		return soundPlayer;
	}
	
	@Override
	public DefaultStepPlayer getDefaultStepPlayer() {
		return defaultStepPlayer;
	}
	
	@Override
	public void setAcoustics(Library acoustics) {
		this.acoustics = acoustics;
	}
	
	@Override
	public void setSolver(Solver solver) {
		this.solver = solver;
	}
	
	@Override
	public void setBlockMap(BlockMap blockMap) {
		this.blockMap = blockMap;
	}
	
	@Override
	public void setPrimitiveMap(PrimitiveMap primitiveMap) {
		this.primitiveMap = primitiveMap;
	}
	
	@Override
	public void setSoundPlayer(SoundPlayer soundPlayer) {
		this.soundPlayer = soundPlayer;
	}
	
	@Override
	public void setDefaultStepPlayer(DefaultStepPlayer defaultStepPlayer) {
		this.defaultStepPlayer = defaultStepPlayer;
	}
	
	@Override
	public void setVariator(Variator var) {
		VAR = var;
		fixVariator(generator);
	}
	
	@Override
	public void setGenerator(Generator generator) {
		this.generator = generator;
		fixVariator(this.generator);
	}
	
	/**
	 * Propagate variators.
	 * 
	 * @param possiblyAVariator
	 */
	private void fixVariator(Object possiblyAVariator) {
		if (possiblyAVariator != null && possiblyAVariator instanceof VariatorSettable) {
			((VariatorSettable) possiblyAVariator).setVariator(VAR);
		}
	}
}
