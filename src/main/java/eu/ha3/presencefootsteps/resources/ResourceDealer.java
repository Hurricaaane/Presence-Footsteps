package eu.ha3.presencefootsteps.resources;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import eu.ha3.presencefootsteps.PresenceFootsteps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.ClientResourcePackContainer;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

class ResourceDealer {
    public static final Identifier pf_pack = new Identifier("presencefootsteps", "pf_pack.json");
    public static final Identifier acoustics = new Identifier("presencefootsteps", "acoustics.json");
    public static final Identifier blockmap = new Identifier("presencefootsteps", "blockmap.cfg");
    public static final Identifier primitivemap = new Identifier("presencefootsteps", "primitivemap.cfg");
    public static final Identifier variator = new Identifier("presencefootsteps", "variator.cfg");

    private final MinecraftClient client = MinecraftClient.getInstance();

    public Stream<ResourcePack> findResourcePacks() {
        return client.getResourcePackContainerManager().getEnabledContainers().stream()
                .map(ClientResourcePackContainer::createResourcePack).filter(this::checkCompatible);
    }

    public Stream<ResourcePack> findDisabledResourcePacks() {
        return client.getResourcePackContainerManager().getDisabledContainers().stream()
                .map(ClientResourcePackContainer::createResourcePack).filter(this::checkCompatible);
    }

    private boolean checkCompatible(ResourcePack pack) {
        return pack.contains(ResourceType.CLIENT_RESOURCES, pf_pack);
    }

    public <T extends Closeable> void collectResources(Identifier key, List<ResourcePack> repo, Consumer<T> consumer,
            Function<InputStream, T> converter) {
        int working = 0;

        for (ResourcePack pack : repo) {

            try (T stream = converter.apply(open(pack, key))) {
                consumer.accept(stream);
            } catch (Exception e) {
                PresenceFootsteps.logger.info("No variator found in " + pack.getName() + ": " + e.getMessage());
            }
            working++;
        }

        if (working == 0) {
            PresenceFootsteps.logger.info("No " + key + " found in " + repo.size() + " packs!");
        }
    }

    public InputStream open(ResourcePack pack, Identifier key) throws IOException {
        return pack.open(ResourceType.CLIENT_RESOURCES, key);
    }
}
