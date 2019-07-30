package eu.ha3.presencefootsteps.game.reader;

import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import eu.ha3.mc.haddon.Utility;
import eu.ha3.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.presencefootsteps.game.interfaces.Isolator;

public class PFReaderQuad extends PFReader {
	private int hoof = 0;
	private int USE_FUNCTION = 2;
	private float nextWalkDistanceMultiplier = 0.05f;
	private final Random rand = new Random();
	
	public PFReaderQuad(Isolator isolator, Utility utility) {
		super(isolator, utility);
	}
	
	@Override
	protected void stepped(EntityPlayer ply, EventType event) {
		if (hoof == 0 || hoof == 2) {
			nextWalkDistanceMultiplier = rand.nextFloat();
		}
		
		if (hoof >= 3) {
			hoof = 0;
		} else {
			hoof++;
		}
		
		if (hoof == 3 && event == EventType.RUN) {
			produceStep(ply, event);
			hoof = 0;
		}
		
		if (event == EventType.WALK) {
			produceStep(ply, event);
		}
	}
	
	@Override
	protected float reevaluateDistance(EventType event, float distance) {
		float ret = distance;
		if (event == EventType.WALK) {
			if (USE_FUNCTION == 2) {
				final float overallMultiplier = 1.85f / 2;
				final float ndm = 0.2f;
				
				/*if (this.hoof == 1 || this.hoof == 3)
					return this.nextWalkDistanceMultiplier * 0.5f;
				else
					return ret * (1 - ndm) * overallMultiplier;*/
				float pond = nextWalkDistanceMultiplier;
				pond *= pond;
				pond *= ndm;
				if (hoof == 1 || hoof == 3) {
					return ret * pond * overallMultiplier;
				}
				return ret * (1 - pond) * overallMultiplier;
			} else if (USE_FUNCTION == 1) {
				final float overallMultiplier = 1.4f;
				final float ndm = 0.5f;
				
				if (hoof == 1 || hoof == 3) {
					return ret * (ndm + nextWalkDistanceMultiplier * ndm * 0.5f) * overallMultiplier;
				}
				return ret * (1 - ndm) * overallMultiplier;
			} else if (USE_FUNCTION == 0) {
				final float overallMultiplier = 1.5f;
				final float ndm = 0.425f + nextWalkDistanceMultiplier * 0.15f;
				
				if (hoof == 1 || hoof == 3) {
					return ret * ndm * overallMultiplier;
				}
				return ret * (1 - ndm) * overallMultiplier;
			}
		}
		
		if (event == EventType.RUN && hoof == 0) {
			return ret * 0.8f;
		}
		
		if (event == EventType.RUN) {
			return ret * 0.3f;
		}
		
		return ret;
	}
	
}
