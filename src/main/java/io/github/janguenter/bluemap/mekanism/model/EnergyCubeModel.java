/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.mekanism.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.bluecolored.bluemap.core.resources.adapter.ResourcesGson;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Installed energy-cube frame, LED and port element groups. */
public record EnergyCubeModel(Map<String, List<Element>> groups) {

    private static final List<String> NAMES = List.of(
            "frame",
            "frontLEDs", "frontPort",
            "leftLEDs", "leftPort",
            "rightLEDs", "rightPort",
            "backLEDs", "backPort",
            "topLEDs", "topPort",
            "bottomLEDs", "bottomPort"
    );

    public EnergyCubeModel {
        groups = Map.copyOf(groups);
    }

    /** Compiles only ordinary element arrays; textures remain operator-installed. */
    public static EnergyCubeModel compile(byte[] raw) throws IOException {
        JsonObject root;
        try (InputStreamReader reader = new InputStreamReader(
                new ByteArrayInputStream(raw), StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonObject()) {
                throw new IOException("energy cube model is not an object");
            }
            root = parsed.getAsJsonObject();
        }
        Map<String, List<Element>> groups = new LinkedHashMap<>();
        for (String name : NAMES) {
            JsonElement rawElements = root.get(name);
            if (rawElements == null || !rawElements.isJsonArray()) {
                throw new IOException("missing energy cube group " + name);
            }
            JsonObject ordinary = new JsonObject();
            ordinary.add("textures", root.get("textures").deepCopy());
            ordinary.add("elements", rawElements.deepCopy());
            Model parsed = ResourcesGson.INSTANCE.fromJson(ordinary, Model.class);
            Element[] elements = parsed == null ? null : parsed.getElements();
            if (elements == null || elements.length == 0) {
                throw new IOException("empty energy cube group " + name);
            }
            List<Element> list = new ArrayList<>(elements.length);
            for (Element element : elements) {
                if (element != null) {
                    list.add(element);
                }
            }
            groups.put(name, List.copyOf(list));
        }
        if (groups.get("frame").size() != 28) {
            throw new IOException("energy cube frame roster changed");
        }
        return new EnergyCubeModel(groups);
    }
}
