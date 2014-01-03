package net.minecraft.src;

import java.util.HashSet;
import java.util.Set;

/* x-placeholder-wtfplv2 */

public class PFCacheRegistry
{
	private Set<String> set;
	
	public PFCacheRegistry()
	{
		this.set = new HashSet<String>();
	}
	
	public void clear()
	{
		this.set.clear();
	}
	
	public void cacheSound(String path)
	{
		if (this.set.contains(path))
			return;
		
		Minecraft.getMinecraft().sndManager.addSound(path);
		this.set.add(path);
		
		//PFHaddon.log("Cached sound " + path);
	}
}
