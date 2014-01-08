package eu.ha3.mc.presencefootsteps.game.system;

import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import eu.ha3.mc.presencefootsteps.engine.interfaces.EventType;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Isolator;

/* x-placeholder-wtfplv2 */

public class PFReaderQP extends PFReaderH
{
	private int hoof = 0;
	private int USE_FUNCTION = 2;
	private float nextWalkDistanceMultiplier = 0.05f;
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
		if (event == EventType.WALK)
		{
			if (this.USE_FUNCTION == 2)
			{
				final float overallMultiplier = 1.85f / 2;
				final float ndm = 0.2f;
				
				/*if (this.hoof == 1 || this.hoof == 3)
					return this.nextWalkDistanceMultiplier * 0.5f;
				else
					return ret * (1 - ndm) * overallMultiplier;*/
				float pond = this.nextWalkDistanceMultiplier;
				pond *= pond;
				pond *= ndm;
				if (this.hoof == 1 || this.hoof == 3)
					return ret * pond * overallMultiplier;
				else
					return ret * (1 - pond) * overallMultiplier;
			}
			else if (this.USE_FUNCTION == 1)
			{
				final float overallMultiplier = 1.4f;
				final float ndm = 0.5f;
				
				if (this.hoof == 1 || this.hoof == 3)
					return ret * (ndm + this.nextWalkDistanceMultiplier * ndm * 0.5f) * overallMultiplier;
				else
					return ret * (1 - ndm) * overallMultiplier;
			}
			else if (this.USE_FUNCTION == 0)
			{
				final float overallMultiplier = 1.5f;
				final float ndm = 0.425f + this.nextWalkDistanceMultiplier * 0.15f;
				
				if (this.hoof == 1 || this.hoof == 3)
					return ret * ndm * overallMultiplier;
				else
					return ret * (1 - ndm) * overallMultiplier;
			}
		}
		
		if (event == EventType.RUN && this.hoof == 0)
			return ret * 0.8f;
		
		if (event == EventType.RUN)
			return ret * 0.3f;
		
		return ret;
	}
	
}
