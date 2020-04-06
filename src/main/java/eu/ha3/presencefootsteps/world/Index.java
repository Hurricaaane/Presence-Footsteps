package eu.ha3.presencefootsteps.world;

public interface Index<K, V> extends Loadable {
    V lookup(K key);
}
