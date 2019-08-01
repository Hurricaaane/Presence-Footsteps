package eu.ha3.presencefootsteps.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerUtil {
    public static boolean isClientPlayer(Entity entity) {
        return resolveToClientPlayer(entity) == entity;
    }

    public static PlayerEntity resolveToClientPlayer(Entity entity) {
        PlayerEntity client = MinecraftClient.getInstance().player;

        if (entity instanceof PlayerEntity) {
            if (client != null && (client == entity || client.getUuid().equals(entity.getUuid()))) {
                return client;
            }

            return (PlayerEntity)entity;
        }

        return client;
    }
}
