package eu.ha3.presencefootsteps.game.implem;

import net.minecraft.block.Block;
import eu.ha3.presencefootsteps.log.PFLog;
import eu.ha3.presencefootsteps.util.PFHelper;

public class LegacyCapableBlockMap extends BasicBlockMap {
	@Override
	public void register(String key, String value) {
		try {
			int endOfNumber = key.indexOf('^');
			if (endOfNumber == -1) {
				endOfNumber = key.indexOf('.');
			}
			if (endOfNumber == -1) {
				endOfNumber = key.length();
			}
			String number = key.substring(0, endOfNumber);
			int id = Integer.parseInt(number);
			Object o = Block.REGISTRY.getObjectById(id);
			if (o != null && o instanceof Block) {
				String fullKeyRebuild = PFHelper.nameOf((Block) o) + (endOfNumber == key.length() ? "" : key.substring(endOfNumber));
				super.register(fullKeyRebuild, value);
				PFLog.debug("Adding legacy key: " + fullKeyRebuild + " for " + key);
			} else {
				super.register(key, value);
			}
		} catch (NumberFormatException e) {
			super.register(key, value);
		}
	}
}
