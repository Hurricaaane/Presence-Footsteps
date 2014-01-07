package eu.ha3.mc.presencefootsteps.game.system;

import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import eu.ha3.mc.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Isolator;

/* x-placeholder-wtfplv2 */

public class PFReaderQP extends PFReaderH
{
	private int hoof = 0;
	private boolean USE_ALTERNATIVE_HOOVES = true;
	private float nextWalkDistanceMultiplier = 0.07f;
	private final Random rand = new Random();
	
	public PFReaderQP(Isolator isolator)
	{
		super(isolator);
	}
	
	@Override
	protected void stepped(EntityPlayer ply, EventType event)
	{
		if (this.hoof == 0 || this.hoof == 2)
		{
			this.nextWalkDistanceMultiplier = this.rand.nextFloat();
		}
		
		if (this.hoof >= 3)
		{
			this.hoof = 0;
		}
		else
		{
			this.hoof = this.hoof + 1;
		}
		
		if (this.hoof == 3 && event == EventType.RUN)
		{
			produceStep(ply, event);
			this.hoof = 0;
		}
		
		if (event == EventType.WALK)
		{
			produceStep(ply, event);
		}
	}
	
	@Override
	protected float reevaluateDistance(EventType event, float distance)
	{
		float ret = distance;
		
		if (this.USE_ALTERNATIVE_HOOVES && event == EventType.WALK)
		{
			final float overallMultiplier = 1f;
			final float ndm = 0.02f + this.nextWalkDistanceMultiplier * 0.07f;
			
			if (this.hoof == 1 || this.hoof == 3)
				return ret * ndm * overallMultiplier;
			else
				return ret * (1 - ndm) * overallMultiplier;
		}
		else if (event == EventType.WALK)
		{
			final float overallMultiplier = 1.5f;
			final float ndm = 0.425f + this.nextWalkDistanceMultiplier * 0.15f;
			
			if (this.hoof == 1 || this.hoof == 3)
				return ret * ndm * overallMultiplier;
			else
				return ret * (1 - ndm) * overallMultiplier;
		}
		
		if (event == EventType.RUN && this.hoof == 0)
			return ret * 0.8f;
		
		if (event == EventType.RUN)
			return ret * 0.3f;
		
		return ret;
	}
	
}
