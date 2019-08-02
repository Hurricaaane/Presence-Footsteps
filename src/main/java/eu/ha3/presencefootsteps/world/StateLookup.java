package eu.ha3.presencefootsteps.world;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import eu.ha3.presencefootsteps.PresenceFootsteps;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * A state lookup that finds an association for a given block state within a specific substrate (or no substrate).
 *
 * @author Sollace
 */
public class StateLookup implements Lookup<BlockState> {

    private final Map<String, Substrate> substrates = new LinkedHashMap<>();

    @Override
    public String getAssociation(BlockState state, String substrate) {

        Substrate sub = substrates.get(substrate);

        if (sub != null) {
            return sub.get(state);
        }

        return Emitter.UNASSIGNED;
    }

    @Override
    public void add(String key, String value) {
        if (!Emitter.isResult(value)) {
            PresenceFootsteps.logger.info("Skipping non-result value " + key + "=" + value);
            return;
        }

        Key k = new Key(key, value);

        substrates.computeIfAbsent(k.substrate, Substrate::new).add(k);
    }

    @Override
    public boolean contains(BlockState state) {

        for (Substrate substrate : substrates.values()) {
            if (Emitter.isResult(substrate.get(state))) {
                return true;
            }
        }

        return false;
    }

    private static class Substrate {

        private final Map<Identifier, Bucket> buckets = new LinkedHashMap<>();
        private final List<Object> values = new LinkedList<>();

        Substrate(String substrate) { }

        void add(Key key) {
            buckets.computeIfAbsent(key.identifier, Bucket::new).keys.add(key);
            values.add(key);
        }

        String get(BlockState state) {

            Bucket bucket = buckets.get(Registry.BLOCK.getId(state.getBlock()));

            if (bucket != null) {
                return bucket.get(state);
            }

            return Emitter.UNASSIGNED;
        }
    }

    private static class Bucket {
        private final Map<BlockState, Key> cache = new LinkedHashMap<>();
        private final List<Key> keys = new LinkedList<>();

        Bucket(Identifier id) { }

        String get(BlockState state) {
            return cache.computeIfAbsent(state, this::computekey).value;
        }

        private Key computekey(BlockState state) {
            for (Key i : keys) {
                if (i.matches(state)) {
                    return i;
                }
            }

            return Key.NULL;
        }
    }

    private static class Key {

        public static final Key NULL = new Key();

        public final Identifier identifier;

        public final String substrate;

        private final Set<Attribute> properties;

        public final String value;

        private final boolean empty;

        private Key() {
            identifier = new Identifier("air");
            substrate = "";
            properties = Collections.emptySet();
            value = Emitter.UNASSIGNED;
            empty = true;
        }

        /*
         * minecraft:block[one=1,two=2].substrate
         */
        Key(String key, String value) {
            String id = key.split("[\\.\\[]")[0];

            this.value = value;

            if (id.indexOf('^') > -1) {
                identifier = new Identifier(id.split("\\^")[0]);
                PresenceFootsteps.logger.warn("Metadata entry for " + key + "=" + value + " was ignored");
            } else {
                identifier = new Identifier(id);
            }

            if (!Registry.BLOCK.containsId(identifier)) {
                PresenceFootsteps.logger.warn("Sound registered for unknown block id " + identifier);
            }

            key = key.replace(id, "");

            String substrate = key.replaceFirst("\\[[^\\]]+\\]", "");

            if (substrate.indexOf('.') > -1) {
                this.substrate = substrate.split("\\.")[1];

                key = key.replace(substrate, "");
            } else {
                this.substrate = "";
            }

            properties = Lists.newArrayList(key.replace("[", "").replace("]", "").split(","))
                .stream()
                .filter(line -> line.indexOf('=') > -1)
                .map(Attribute::new)
                .collect(Collectors.toSet());
            empty = properties.isEmpty();
        }

        boolean matches(BlockState state) {

            if (empty) {
                return true;
            }

            Map<Property<?>, Comparable<?>> entries = state.getEntries();
            Set<Property<?>> keys = entries.keySet();

            for (Attribute property : properties) {
                for (Property<?> key : keys) {
                    if (key.getName().equals(property.name)) {
                        Comparable<?> value = entries.get(key);

                        if (!Objects.toString(value).equals(property.value)) {
                            return false;
                        }
                    }
                }
            }

            return true;
        }

        private static class Attribute {
            private final String name;
            private final String value;

            Attribute(String prop) {
                String[] split = prop.split("=");

                this.name = split[0];
                this.value = split[1];
            }
        }
    }
}
