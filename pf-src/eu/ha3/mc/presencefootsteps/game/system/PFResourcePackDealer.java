package eu.ha3.mc.presencefootsteps.game.system;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.ResourceLocation;

/*
--filenotes-placeholder
*/

public class PFResourcePackDealer
{
	private final ResourceLocation pf_pack = new ResourceLocation("presencefootsteps", "pf_pack.json");
	private final ResourceLocation acoustics = new ResourceLocation("presencefootsteps", "acoustics.json");
	private final ResourceLocation blockmap = new ResourceLocation("presencefootsteps", "blockmap.cfg");
	private final ResourceLocation primitivemap = new ResourceLocation("presencefootsteps", "primitivemap.cfg");
	private final ResourceLocation variator = new ResourceLocation("presencefootsteps", "variator.cfg");
	
	public List<ResourcePackRepository.Entry> findResourcePacks()
	{
		@SuppressWarnings("unchecked")
		List<ResourcePackRepository.Entry> repo =
			Minecraft.getMinecraft().getResourcePackRepository().getRepositoryEntries();
		
		List<ResourcePackRepository.Entry> foundEntries = new ArrayList<ResourcePackRepository.Entry>();
		
		for (ResourcePackRepository.Entry pack : repo)
		{
			if (checkCompatible(pack))
			{
				foundEntries.add(pack);
			}
		}
		return foundEntries;
	}
	
	private boolean checkCompatible(ResourcePackRepository.Entry pack)
	{
		/*try
		{
			InputStream is = pack.getResourcePack().getInputStream(this.pf_pack);
			is.close();
			return true;
		}
		catch (IOException e)
		{
			return false;
		}*/
		return pack.getResourcePack().resourceExists(this.pf_pack);
	}
	
	public InputStream openPackDescriptor(IResourcePack pack) throws IOException
	{
		InputStream is = pack.getInputStream(this.pf_pack);
		return is;
	}
	
	public InputStream openAcoustics(IResourcePack pack) throws IOException
	{
		InputStream is = pack.getInputStream(this.acoustics);
		return is;
	}
	
	public InputStream openBlockMap(IResourcePack pack) throws IOException
	{
		InputStream is = pack.getInputStream(this.blockmap);
		return is;
	}
	
	public InputStream openPrimitiveMap(IResourcePack pack) throws IOException
	{
		InputStream is = pack.getInputStream(this.primitivemap);
		return is;
	}
	
	public InputStream openVariator(IResourcePack pack) throws IOException
	{
		InputStream is = pack.getInputStream(this.variator);
		return is;
	}
}
