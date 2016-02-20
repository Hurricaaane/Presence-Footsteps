package eu.ha3.presencefootsteps.util;

import com.minelittlepony.minelp.Pony;
import com.minelittlepony.minelp.PonyLevel;
import com.minelittlepony.minelp.PonyManager;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class MineLittlePonyCommunicator {
	
	private static boolean arePoniesShown() {
		try {
			PonyLevel level = PonyManager.getInstance().getPonyLevel();
			return level == PonyLevel.PONIES || level == PonyLevel.MIXED;
		} catch (Throwable e) {
			return false;
		}
	}
	
	public static int getEffectiveRace() {
		if (arePoniesShown()) {
			try {
				EntityPlayer ply = Minecraft.getMinecraft().thePlayer;
				Pony pony = PonyManager.getInstance().getPonyFromResourceRegistry(ply);
				return pony.isPegasus() ? 2 : 1;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
}
