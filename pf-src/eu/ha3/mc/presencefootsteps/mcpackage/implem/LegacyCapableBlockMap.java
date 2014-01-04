package eu.ha3.mc.presencefootsteps.mcpackage.implem;

import net.minecraft.block.Block;
import net.minecraft.src.PFHaddon;
import eu.ha3.mc.presencefootsteps.modplants.PF172Helper;

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
			Object o = Block.field_149771_c.func_148754_a(id);
			if (o != null && o instanceof Block)
			{
				String fullKeyRebuild =
					PF172Helper.nameOf((Block) o) + (endOfNumber == key.length() ? "" : key.substring(endOfNumber));
				super.register(fullKeyRebuild, value);
				PFHaddon.debug("Adding legacy key: " + fullKeyRebuild + " for " + key);
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
