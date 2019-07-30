package eu.ha3.presencefootsteps.log;

import eu.ha3.presencefootsteps.util.PFHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

public class PFLog {
	private static boolean isDebugEnabled;
	
	public static void logf(String contents, Object... params) {
		int contentIndex = 0;
		for (int i = 0; i < params.length; i++) {
			if (params[i] instanceof IBlockState) {
				contents = contents.replace("%" + contentIndex++, ((IBlockState)params[i]).getBlock().toString());
				contents = contents.replace("%" + contentIndex++, ((IBlockState)params[i]).getProperties().toString());
			} else if (params[i] instanceof Block) {
				contents = contents.replace("%" + contentIndex++, PFHelper.nameOf((Block)params[i]));
			} else {
				contents = contents.replace("%" + contentIndex++, params[i].toString());
			}
		}
		log(contents);
	}
	
	public static void log(String contents) {
		System.out.println("[PF]: " + contents);
	}
	
	public static void setDebugEnabled(boolean enable) {
		isDebugEnabled = enable;
	}
	
	public static boolean getDebugEnabled() {
		return isDebugEnabled;
	}
	
	public static void debugf(String contents, Object... params) {
		if (isDebugEnabled) {
			logf(contents, params);
		}
	}
	
	public static void debug(String contents) {
		if (isDebugEnabled) {
			log(contents);
		}
	}
}
