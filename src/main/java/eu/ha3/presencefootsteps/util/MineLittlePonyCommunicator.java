package eu.ha3.presencefootsteps.util;

import com.brohoof.minelittlepony.MineLittlePony;
import com.brohoof.minelittlepony.Pony;
import com.brohoof.minelittlepony.PonyLevel;

import com.sollace.unicopia.Race;
import com.sollace.unicopia.server.PlayerSpeciesRegister;

import eu.ha3.mc.convenience.Ha3StaticUtilities;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;

public class MineLittlePonyCommunicator {
	public static final MineLittlePonyCommunicator instance = new MineLittlePonyCommunicator();
	
	private static boolean unicopiaDetected = Ha3StaticUtilities.classExists("com.sollace.unicopia.Race", instance);
	private static boolean MineLPDetected = Ha3StaticUtilities.classExists("com.brohoof.minelittlepony.LiteModMineLittlePony", instance);
	
	private boolean arePoniesShown() {
		if (!MineLPDetected) return false;
		try {
			PonyLevel level = MineLittlePony.getInstance().getConfig().getPonyLevel();
			return level == PonyLevel.PONIES || level == PonyLevel.BOTH;
		} catch (Throwable e) {
			e.printStackTrace();
			return MineLPDetected = false;
		}
	}
	
	public boolean ponyModInstalled() {
		return MineLPDetected || unicopiaDetected;
	}
	
	public int getEffectiveRace(EntityPlayer ply) {
		//return pegasus if Unicopia is installed and player's current race can fly
		//overrides MineLP setting
		if (unicopiaDetected) {
			try {
				Race race = PlayerSpeciesRegister.getPlayerSpecies(ply);
				if (race != null && race.canFly()) return 2;
			} catch (Throwable e) {
				e.printStackTrace();
				unicopiaDetected = false;
			}
		}
		//get type of stance (4 legged / pegasus) if MineLP is installed
		if (arePoniesShown() && ply instanceof AbstractClientPlayer) {
			Pony pony = MineLittlePony.getInstance().getManager().getPonyFromResourceRegistry((AbstractClientPlayer)ply);
			return pony.isPegasus() ? 2 : 1;
		}
		return 0;
	}
}
