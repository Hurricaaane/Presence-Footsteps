package eu.ha3.mc.presencefootsteps.modplants;

import java.io.IOException;
import java.io.InputStream;

import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.ResourceLocation;

/*
--filenotes-placeholder
*/

public class ResourcePackDealer
{
	private final ResourceLocation pf_pack = new ResourceLocation("presencefootsteps", "pf_pack.json");
	private final ResourceLocation acoustics = new ResourceLocation("presencefootsteps", "acoustics.json");
	private final ResourceLocation blockmap = new ResourceLocation("presencefootsteps", "blockmap.cfg");
	private final ResourceLocation primitivemap = new ResourceLocation("presencefootsteps", "primitivemap.cfg");
	private final ResourceLocation variator = new ResourceLocation("presencefootsteps", "variator.cfg");
	
	public boolean checkoutPresencePack(ResourcePackRepository.Entry pack)
	{
		try
		{
			InputStream is = pack.getResourcePack().getInputStream(this.pf_pack);
			is.close();
			return true;
		}
		catch (IOException e)
		{
			return false;
		}
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
