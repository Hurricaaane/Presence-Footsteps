package net.minecraft.src;

import java.util.HashSet;
import java.util.Set;

/* x-placeholder-wtfplv2 */

@Deprecated
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
	
	@Deprecated
	public void cacheSound(String path)
	{
		if (this.set.contains(path))
			return;
		
		// XXX 2014-01-04 : not working anymore
		//Minecraft.getMinecraft().sndManager.addSound(path);
		this.set.add(path);
		
		//PFHaddon.log("Cached sound " + path);
	}
}
