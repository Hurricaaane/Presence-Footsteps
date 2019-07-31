package eu.ha3.presencefootsteps.world;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.util.registry.Registry;

public class StateLookup implements Lookup<BlockState> {

    private final Map<String, String> blockMap = new LinkedHashMap<>();

    @Override
    @Nullable
    public String getAssociation(BlockState state) {
        return blockMap.get(state.toString());
    }

    @Override
    @Nullable
    public String getAssociation(BlockState state, String substrate) {
        String key = state.toString() + "." + substrate;

        if (blockMap.containsKey(key)) {
            return blockMap.get(key);
        }

        return getAssociation(state);
    }

    @Override
    public void add(String key, String value) {
        blockMap.put(key.replace('>', ':'), value);
    }

    @Override
    public boolean contains(BlockState state) {
        String key = Registry.BLOCK.getId(state.getBlock()).toString();

        return blockMap.entrySet().stream().anyMatch(i -> i.getKey().startsWith(key));
    }
}
