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
	public String getBlockMaterial(IBlockState state) {
		return getForMapping(PF172Helper.nameOf(state.getBlock()) + "^" + state.getBlock().getMetaFromState(state));
	}
	
	@Override
	public String getBlockMapSubstrate(IBlockState state, String substrate) {
		return getForMapping(PF172Helper.nameOf(state.getBlock()) + "^" + state.getBlock().getMetaFromState(state) + "." + substrate);
	}
	
	private String getForMapping(String mapping) {
		String material = null;
		if (this.blockMap.containsKey(mapping)) {
			material = this.blockMap.get(mapping);
		} else if (this.blockMap.containsKey(mapping)) {
			material = this.blockMap.get(mapping);
		}
		return material;
	}
	
	@Override
	public void register(String key, String value) {
		blockMap.put(key.replace('>', ':'), value);
	}
}
