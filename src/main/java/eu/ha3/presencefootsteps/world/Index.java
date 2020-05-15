package eu.ha3.presencefootsteps.world;

import net.minecraft.util.Identifier;

public interface Index<K, V> extends Loadable {
    V lookup(K key);

    boolean contains(Identifier key);
}
