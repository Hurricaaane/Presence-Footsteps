package eu.ha3.presencefootsteps.game;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import eu.ha3.presencefootsteps.engine.interfaces.Library;
import eu.ha3.presencefootsteps.engine.interfaces.SoundPlayer;
import eu.ha3.presencefootsteps.game.interfaces.BlockMap;
import eu.ha3.presencefootsteps.game.interfaces.DefaultStepPlayer;
import eu.ha3.presencefootsteps.game.interfaces.Generator;
import eu.ha3.presencefootsteps.game.interfaces.Isolator;
import eu.ha3.presencefootsteps.game.interfaces.PrimitiveMap;
import eu.ha3.presencefootsteps.game.interfaces.Solver;
import eu.ha3.presencefootsteps.game.interfaces.Variator;
import eu.ha3.presencefootsteps.game.interfaces.VariatorSettable;
import eu.ha3.presencefootsteps.main.PFHaddon;

import net.minecraft.entity.player.EntityPlayer;

public class PFIsolator implements Isolator, VariatorSettable {
	
	private final Map<UUID, Generator> generators = new HashMap<UUID, Generator>();
	private final PFHaddon mod;
	
	private Library acoustics;
	private Solver solver;
	private BlockMap blockMap;
	private PrimitiveMap primitiveMap;
	private SoundPlayer soundPlayer;
	private DefaultStepPlayer defaultStepPlayer;
	
	private Variator VAR;
	
	public PFIsolator(PFHaddon mod) {
		this.mod = mod;
	}
	
	private Generator getGenerator(EntityPlayer ply) {
		if (generators.size() > 10) {
			Iterator<Map.Entry<UUID, Generator>> iter = generators.entrySet().iterator();
			while (iter.hasNext()) {
				UUID uuid = iter.next().getKey();
				if (ply.worldObj.getPlayerEntityByUUID(uuid) == null) {
					iter.remove();
				}
			}
		}
		UUID key = ply.getUniqueID();
		if (generators.containsKey(key)) {
			return generators.get(ply.getUniqueID());
		}
		Generator gen = mod.getReader(ply);
		if (gen != null) {
			generators.put(key, gen);
			fixVariator(gen);
		}
		return gen;
	}
	
	@Override
	public void reload() {
		generators.clear();
	}
	
	@Override
	public void onFrame(EntityPlayer ply) {
		if (ply == null || ply.isDead) return;
		Generator gen = getGenerator(ply);
		if (gen == null) return;
		
		gen.generateFootsteps(ply);
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
		reload();
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
