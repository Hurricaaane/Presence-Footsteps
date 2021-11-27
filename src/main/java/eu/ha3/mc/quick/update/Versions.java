package eu.ha3.mc.quick.update;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.util.JsonHelper;

public record Versions (
        TargettedVersion latest,
        List<TargettedVersion> previous) {

    public Versions(JsonObject json) throws VersionParsingException {
        this(new TargettedVersion(JsonHelper.getObject(json, "latest")), new ArrayList<>());
        for (var el : JsonHelper.getArray(json, "previous")) {
            previous.add(new TargettedVersion(el.getAsJsonObject()));
        }
    }
}
