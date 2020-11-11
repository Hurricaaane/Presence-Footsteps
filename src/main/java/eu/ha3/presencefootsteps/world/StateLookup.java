package eu.ha3.presencefootsteps.world;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import eu.ha3.presencefootsteps.PresenceFootsteps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * A state lookup that finds an association for a given block state within a specific substrate (or no substrate).
 *
 * @author Sollace
 */
public class StateLookup implements Lookup<BlockState> {

    private final Map<String, Bucket> substrates = new LinkedHashMap<>();

    @Override
    public String getAssociation(BlockState state, String substrate) {
        return substrates.getOrDefault(substrate, Bucket.EMPTY).get(state).value;
    }

    @Override
    public void add(String key, String value) {
        if (!Emitter.isResult(value)) {
            PresenceFootsteps.logger.info("Skipping non-result value " + key + "=" + value);
            return;
        }

        Key k = new Key(key, value);

        substrates.computeIfAbsent(k.substrate, Bucket.Substrate::new).add(k);
    }

    @Override
    public Set<String> getSubstrates() {
        return substrates.keySet();
    }

    @Override
    public boolean contains(BlockState state) {
        for (Bucket substrate : substrates.values()) {
            if (substrate.get(state) != Key.NULL) {
                return true;
            }
        }

        return false;
    }

    private interface Bucket {

        Bucket EMPTY = state -> Key.NULL;

        default void add(Key key) {}

        Key get(BlockState state);

        final class Substrate implements Bucket {
            private final KeyList wildcards = new KeyList();
            private final Map<Identifier, Bucket> blocks = new LinkedHashMap<>();
            private final Map<Identifier, Bucket> tags = new LinkedHashMap<>();

            Substrate(String substrate) { }

            @Override
            public void add(Key key) {
                if (key.isWildcard) {
                    wildcards.add(key);
                } else {
                    (key.isTag ? tags : blocks).computeIfAbsent(key.identifier, Tile::new).add(key);
                }
            }

            @Override
            public Key get(BlockState state) {
                Key association = blocks.computeIfAbsent(Registry.BLOCK.getId(state.getBlock()), this::computeTile).get(state);

                if (association == Key.NULL) {
                    return wildcards.findMatch(state);
                }
                return association;
            }

            private Bucket computeTile(Identifier id) {
                Block block = Registry.BLOCK.get(id);

                for (Identifier tag : tags.keySet()) {
                    if (BlockTags.getTagGroup().getTagOrEmpty(tag).contains(block)) {
                        return tags.get(tag);
                    }
                }

                return Bucket.EMPTY;
            }
        }

        final class Tile implements Bucket {
            private final Map<BlockState, Key> cache = new LinkedHashMap<>();
            private final KeyList keys = new KeyList();

            Tile(Identifier id) { }

            @Override
            public void add(Key key) {
                keys.add(key);
            }

            @Override
            public Key get(BlockState state) {
                return cache.computeIfAbsent(state, keys::findMatch);
            }
        }
    }

    private static final class KeyList extends LinkedList<Key> {
        private static final long serialVersionUID = -7880900110753930099L;

        public Key findMatch(BlockState state) {
            for (Key i : this) {
                if (i.matches(state)) {
                    return i;
                }
            }
            return Key.NULL;
        }
    }

    private static final class Key {

        public static final Key NULL = new Key();

        public final Identifier identifier;

        public final String substrate;

        private final Set<Attribute> properties;

        public final String value;

        private final boolean empty;

        public final boolean isTag;

        public final boolean isWildcard;

        private Key() {
            identifier = new Identifier("air");
            substrate = "";
            properties = Collections.emptySet();
            value = Emitter.UNASSIGNED;
            empty = true;
            isTag = false;
            isWildcard = false;
        }

        /*
         * minecraft:block[one=1,two=2].substrate
         * #minecraft:blanks[one=1,two=2].substrate
         */
        Key(String key, String value) {

            this.value = value;
            this.isTag = key.indexOf('#') == 0;

            if (isTag) {
                key = key.replaceFirst("#", "");
            }

            String id = key.split("[\\.\\[]")[0];

            isWildcard = id.indexOf('*') == 0;

            if (!isWildcard) {
                if (id.indexOf('^') > -1) {
                    identifier = new Identifier(id.split("\\^")[0]);
                    PresenceFootsteps.logger.warn("Metadata entry for " + key + "=" + value + " was ignored");
                } else {
                    identifier = new Identifier(id);
                }

                if (!isTag && !Registry.BLOCK.containsId(identifier)) {
                    PresenceFootsteps.logger.warn("Sound registered for unknown block id " + identifier);
                }
            } else {
                identifier = new Identifier("air");
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
