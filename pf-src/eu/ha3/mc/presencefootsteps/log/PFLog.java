package eu.ha3.mc.presencefootsteps.log;

/*
--filenotes-placeholder
*/

public class PFLog {
	private static boolean isDebugEnabled;
	
	public static void log(String contents) {
		System.out.println("(PF) " + contents);
	}
	
	public static void setDebugEnabled(boolean enable) {
		isDebugEnabled = enable;
	}
	
	public static void debug(String contents) {
		if (isDebugEnabled) log(contents);
	}
}
