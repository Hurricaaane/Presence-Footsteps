package eu.ha3.presencefootsteps;

import com.minelittlepony.client.MineLittlePony;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;

public class MineLP {
    private static boolean checkCompleted = false;
    private static boolean hasMineLP;

    public static boolean hasPonies() {
        if (!checkCompleted) {
            checkCompleted = true;
            hasMineLP = FabricLoader.getInstance().isModLoaded("minelp");
        }

        return hasMineLP;
    }

    public static Locomotion getLocomotion(PlayerEntity ply) {
        return MineLittlePony.getInstance().getManager().getPony(ply).canFly() ? Locomotion.FLYING : Locomotion.QUADRAPED;
    }
}
