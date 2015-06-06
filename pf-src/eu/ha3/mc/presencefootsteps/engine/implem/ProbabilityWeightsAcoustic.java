package eu.ha3.mc.presencefootsteps.engine.implem;

import java.util.ArrayList;
import java.util.List;

import eu.ha3.mc.presencefootsteps.engine.interfaces.Acoustic;
import eu.ha3.mc.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.mc.presencefootsteps.engine.interfaces.Options;
import eu.ha3.mc.presencefootsteps.engine.interfaces.SoundPlayer;
import eu.ha3.mc.presencefootsteps.log.PFLog;

public class ProbabilityWeightsAcoustic implements Acoustic {
	protected List<Acoustic> theAcoustics;
	protected float probabilityThresholds[];
	protected boolean isUseable;
	
	public ProbabilityWeightsAcoustic(List<Acoustic> acoustics, List<Integer> weights) {
		theAcoustics = new ArrayList<Acoustic>(acoustics);
		probabilityThresholds = new float[acoustics.size() - 1];
		
		float total = 0;
		for (int i = 0; i < weights.size(); i++) {
			if (weights.get(i) < 0) {
				PFLog.log("ERROR: A probability weight can't be negative");
				return;
			}
			total = total + weights.get(i);
		}
		
		for (int i = 0; i < weights.size() - 1; i++) {
			probabilityThresholds[i] = weights.get(i) / total;
		}
		
		isUseable = true;
	}
	
	@Override
	public void playSound(SoundPlayer player, Object location, EventType event, Options inputOptions) {
		if (isUseable) {
			float rand = player.getRNG().nextFloat();
			int marker = 0;
			while (marker < probabilityThresholds.length && probabilityThresholds[marker] < rand) {
				marker++;
			}
			theAcoustics.get(marker).playSound(player, location, event, inputOptions);
		}
	}
}
