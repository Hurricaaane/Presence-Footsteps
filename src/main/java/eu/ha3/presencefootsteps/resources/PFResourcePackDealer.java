package eu.ha3.presencefootsteps.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.ResourceLocation;

public class PFResourcePackDealer {
	private final ResourceLocation pf_pack = new ResourceLocation("presencefootsteps", "pf_pack.json");
	private final ResourceLocation acoustics = new ResourceLocation("presencefootsteps", "acoustics.json");
	private final ResourceLocation blockmap = new ResourceLocation("presencefootsteps", "blockmap.cfg");
	private final ResourceLocation primitivemap = new ResourceLocation("presencefootsteps", "primitivemap.cfg");
	private final ResourceLocation variator = new ResourceLocation("presencefootsteps", "variator.cfg");
	
	public List<ResourcePackRepository.Entry> findResourcePacks() {
		List<ResourcePackRepository.Entry> repo = Minecraft.getMinecraft().getResourcePackRepository().getRepositoryEntries();
		return getCompatibleEntries(repo);
	}
	
	public List<ResourcePackRepository.Entry> findDisabledResourcePacks() {
		ResourcePackRepository rrr = Minecraft.getMinecraft().getResourcePackRepository();
		List<ResourcePackRepository.Entry> repo = new ArrayList<ResourcePackRepository.Entry>(rrr.getRepositoryEntriesAll());
		repo.removeAll(rrr.getRepositoryEntries());
		return getCompatibleEntries(repo);
	}
	
	private List<ResourcePackRepository.Entry> getCompatibleEntries(List<ResourcePackRepository.Entry> repo) {
		List<ResourcePackRepository.Entry> result = new ArrayList<ResourcePackRepository.Entry>();
		for (ResourcePackRepository.Entry pack : repo) {
			if (checkCompatible(pack)) {
				result.add(pack);
			}
		}
		return result;
	}
	
	private boolean checkCompatible(ResourcePackRepository.Entry pack) {
		return pack.getResourcePack().resourceExists(pf_pack);
	}
	
	public InputStream openPackDescriptor(IResourcePack pack) throws IOException {
		return pack.getInputStream(pf_pack);
	}
	
	public InputStream openAcoustics(IResourcePack pack) throws IOException {
		return pack.getInputStream(acoustics);
	}
	
	public InputStream openBlockMap(IResourcePack pack) throws IOException {
		return pack.getInputStream(blockmap);
	}
	
	public InputStream openPrimitiveMap(IResourcePack pack) throws IOException {
		return pack.getInputStream(primitivemap);
	}
	
	public InputStream openVariator(IResourcePack pack) throws IOException {
		return pack.getInputStream(variator);
	}
}
