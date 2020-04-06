package eu.ha3.presencefootsteps.world;

import java.util.LinkedHashMap;
import java.util.Map;

import eu.ha3.presencefootsteps.PresenceFootsteps;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class LocomotionLookup implements Index<Entity, Locomotion> {

    private final Map<Identifier, Locomotion> values = new LinkedHashMap<>();

    @Override
    public Locomotion lookup(Entity key) {
        if (key instanceof PlayerEntity) {
            return Locomotion.forPlayer((PlayerEntity)key, Locomotion.NONE);
        }
        return values.getOrDefault(EntityType.getId(key.getType()), Locomotion.BIPED);
    }

    @Override
    public void add(String key, String value) {
        Identifier id = new Identifier(key);

        if (!Registry.ENTITY_TYPE.containsId(id)) {
            PresenceFootsteps.logger.warn("Locomotion registered for unknown entity type " + id);
        }

        values.put(id, Locomotion.byName(value.toUpperCase()));
    }
}
