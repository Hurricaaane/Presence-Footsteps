package eu.ha3.presencefootsteps.util;

import com.minelittlepony.minelp.Pony;
import com.minelittlepony.minelp.PonyLevel;
import com.minelittlepony.minelp.PonyManager;
import com.sollace.unicopia.Race;
import com.sollace.unicopia.server.PlayerSpeciesRegister;

import eu.ha3.mc.convenience.Ha3StaticUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class MineLittlePonyCommunicator {
	public static final MineLittlePonyCommunicator instance = new MineLittlePonyCommunicator();
	
	private static boolean unicopiaDetected = Ha3StaticUtilities.classExists("com.sollace.unicopia.Race", instance);
	private static boolean MineLPDetected = Ha3StaticUtilities.classExists("com.minelittlepony.minelp.Pony", instance);
	
	private boolean arePoniesShown() {
		if (!MineLPDetected) return false;
		try {
			PonyLevel level = PonyManager.getInstance().getPonyLevel();
			return level == PonyLevel.PONIES || level == PonyLevel.MIXED;
		} catch (Throwable e) {
			e.printStackTrace();
			return MineLPDetected = false;
		}
	}
	
	public boolean ponyModInstalled() {
		return MineLPDetected || unicopiaDetected;
	}
	
	public int getEffectiveRace() {
		EntityPlayer ply = Minecraft.getMinecraft().thePlayer;
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
		if (arePoniesShown()) {
			Pony pony = PonyManager.getInstance().getPonyFromResourceRegistry(ply);
			return pony.isPegasus() ? 2 : 1;
		}
		return 0;
	}
}
