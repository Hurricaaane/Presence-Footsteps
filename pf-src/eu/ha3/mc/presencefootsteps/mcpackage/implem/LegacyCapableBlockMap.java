package eu.ha3.mc.presencefootsteps.mcpackage.implem;

import net.minecraft.block.Block;
import eu.ha3.mc.presencefootsteps.game.system.PF172Helper;
import eu.ha3.mc.presencefootsteps.log.PFLog;

/*
--filenotes-placeholder
*/

public class LegacyCapableBlockMap extends BasicBlockMap
{
	@Override
	public void register(String key, String value)
	{
		try
		{
			int endOfNumber = key.indexOf('^');
			if (endOfNumber == -1)
			{
				endOfNumber = key.indexOf('.');
			}
			if (endOfNumber == -1)
			{
				endOfNumber = key.length();
			}
			String number = key.substring(0, endOfNumber);
			int id = Integer.parseInt(number);
			Object o = Block.blockRegistry.getObjectById(id);
			if (o != null && o instanceof Block)
			{
				String fullKeyRebuild =
					PF172Helper.nameOf((Block) o) + (endOfNumber == key.length() ? "" : key.substring(endOfNumber));
				super.register(fullKeyRebuild, value);
				PFLog.debug("Adding legacy key: " + fullKeyRebuild + " for " + key);
			}
			else
			{
				super.register(key, value);
			}
		}
		catch (NumberFormatException e)
		{
			super.register(key, value);
		}
	}
}
