package eu.ha3.mc.presencefootsteps.mcpackage.implem;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import eu.ha3.mc.presencefootsteps.game.system.PF172Helper;
import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.BlockMap;

/* x-placeholder-wtfplv2 */

public class BasicBlockMap implements BlockMap {
	private Map<String, String> blockMap;
	
	public BasicBlockMap() {
		blockMap = new LinkedHashMap<String, String>();
	}
	
	@Override
	public String getBlockMap(IBlockState state) {
		String blockName = PF172Helper.nameOf(state.getBlock());
		int meta = state.getBlock().getMetaFromState(state);
		if (blockMap.containsKey(blockName + "^" + meta)) {
			return blockMap.get(blockName + "^" + meta);
		} else if (blockMap.containsKey(blockName)) {
			return blockMap.get(blockName);
		}
		return null;
	}
	
	@Override
	public String getBlockMapSubstrate(IBlockState state, String substrate) {
		String blockName = PF172Helper.nameOf(state.getBlock());
		int meta = state.getBlock().getMetaFromState(state);
		if (blockMap.containsKey(blockName + "^" + meta + "." + substrate)) {
			return blockMap.get(blockName + "^" + meta + "." + substrate);
		} else if (blockMap.containsKey(blockName + "." + substrate)) {
			return blockMap.get(blockName + "." + substrate);
		}
		return null;
	}
	
	@Override
	public void register(String key, String value) {
		blockMap.put(key.replace('>', ':'), value);
	}
}
