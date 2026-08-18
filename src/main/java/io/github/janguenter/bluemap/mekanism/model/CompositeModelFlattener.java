/*
 * SPDX-License-Identifier: MIT
 *
 * This interpreter follows BlueMap's MIT resource-model objects and the public
 * NeoForge composite JSON shape. It does not copy candidate source code.
 */
package io.github.janguenter.bluemap.mekanism.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.adapter.ResourcesGson;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Rotation;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.TextureVariable;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import com.flowpowered.math.vector.Vector3f;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Flattens installed {@code neoforge:composite} models into ordinary elements. */
public final class CompositeModelFlattener {

    private static final String LOADER = "neoforge:composite";

    private final ResourcePack resourcePack;
    private final Map<Key, JsonObject> definitions;
    private final Map<Key, Model> compiled = new HashMap<>();
    private final Set<Key> resolving = new HashSet<>();
    private final Set<Key> textures = new HashSet<>();

    private CompositeModelFlattener(ResourcePack resourcePack, Map<Key, JsonObject> definitions) {
        this.resourcePack = resourcePack;
        this.definitions = definitions;
    }

    /** Scans the exact installed artifacts and replaces every composite model in-place. */
    public static Result flatten(ResourcePack resourcePack, Iterable<Path> artifacts)
            throws IOException {
        Map<Key, JsonObject> definitions = loadDefinitions(artifacts);
        CompositeModelFlattener flattener = new CompositeModelFlattener(resourcePack, definitions);
        for (Key key : definitions.keySet()) {
            Model model = flattener.compileKey(key);
            resourcePack.getModels().put(key, model);
        }
        return new Result(definitions.size(), Set.copyOf(flattener.textures));
    }

    private Model compileKey(Key key) throws IOException {
        Model ready = compiled.get(key);
        if (ready != null) {
            return ready;
        }
        if (!resolving.add(key)) {
            throw new IOException("composite parent cycle at " + key.getFormatted());
        }
        try {
            JsonObject definition = definitions.get(key);
            if (definition == null) {
                throw new IOException("missing composite " + key.getFormatted());
            }
            Model result = compileInline(definition);
            compiled.put(key, result);
            resourcePack.getModels().put(key, result);
            return result;
        } finally {
            resolving.remove(key);
        }
    }

    private Model compileInline(JsonObject json) throws IOException {
        List<Element> elements = new ArrayList<>();

        JsonObject ordinary = json.deepCopy();
        ordinary.remove("loader");
        ordinary.remove("children");
        prepareCompositeParent(ordinary);
        appendOrdinary(ordinary, elements, null);

        JsonObject children = object(json.get("children"));
        if (children != null) {
            for (Map.Entry<String, JsonElement> entry : children.entrySet()) {
                if (!entry.getValue().isJsonObject()) {
                    throw new IOException("non-object composite child " + entry.getKey());
                }
                JsonObject child = entry.getValue().getAsJsonObject();
                if (child.has("children") || LOADER.equals(string(child.get("loader")))) {
                    Model nested = compileInline(child);
                    appendNormalized(nested, elements, null);
                } else {
                    JsonObject copy = child.deepCopy();
                    copy.remove("loader");
                    Rotation transform = childTransform(copy);
                    copy.remove("transform");
                    prepareCompositeParent(copy);
                    appendOrdinary(copy, elements, transform);
                }
            }
        }

        if (elements.isEmpty()) {
            throw new IOException("composite model has no ordinary geometry");
        }
        return new Model(Map.of(), elements.toArray(Element[]::new));
    }

    private void prepareCompositeParent(JsonObject json) throws IOException {
        String parent = string(json.get("parent"));
        if (parent == null) {
            return;
        }
        Key parentKey = Key.parse(parent);
        if (definitions.containsKey(parentKey)) {
            resourcePack.getModels().put(parentKey, compileKey(parentKey));
        }
    }

    private void appendOrdinary(
            JsonObject json,
            List<Element> target,
            Rotation transform
    ) throws IOException {
        Model model = ResourcesGson.INSTANCE.fromJson(json, Model.class);
        if (model == null) {
            throw new IOException("failed to parse inline model");
        }
        model.applyParent(resourcePack.getModels());
        appendNormalized(model, target, transform);
    }

    private void appendNormalized(
            Model model,
            List<Element> target,
            Rotation transform
    ) throws IOException {
        Element[] source = model.getElements();
        if (source == null) {
            return;
        }
        for (Element element : source) {
            if (element == null) {
                continue;
            }
            EnumMap<Direction, Face> faces = new EnumMap<>(Direction.class);
            for (Map.Entry<Direction, Face> faceEntry : element.getFaces().entrySet()) {
                Face face = faceEntry.getValue();
                if (face == null) {
                    continue;
                }
                ResourcePath<Texture> texture = face.getTexture()
                        .getTexturePath(model.getTextures()::get);
                if (texture == null) {
                    // One exact MoreMachine lathing face references its own absent
                    // #missing variable. Keep the rest of that model instead of
                    // deactivating the complete family for one broken underside.
                    continue;
                }
                textures.add(texture);
                faces.put(faceEntry.getKey(), new Face(
                        face.getUv(),
                        new TextureVariable(texture),
                        face.getCullface(),
                        face.getRotation(),
                        face.getTintindex()
                ));
            }
            target.add(new Element(
                    element.getFrom(),
                    element.getTo(),
                    transform == null ? element.getRotation() : transform,
                    element.isShade(),
                    element.getLightEmission(),
                    faces
            ));
        }
    }

    /** Handles the sole exact placed child transform without inventing a general composer. */
    private static Rotation childTransform(JsonObject child) throws IOException {
        JsonObject transform = object(child.get("transform"));
        if (transform == null) {
            return null;
        }
        JsonElement rotation = transform.get("rotation");
        String origin = string(transform.get("origin"));
        if (rotation == null || !rotation.isJsonArray()
                || rotation.getAsJsonArray().size() != 3
                || (origin != null && !"center".equals(origin))) {
            throw new IOException("unsupported composite child transform");
        }
        float x = rotation.getAsJsonArray().get(0).getAsFloat();
        float y = rotation.getAsJsonArray().get(1).getAsFloat();
        float z = rotation.getAsJsonArray().get(2).getAsFloat();
        if (x != 45F || y != 0F || z != -45F) {
            throw new IOException("unexpected composite child transform");
        }
        return new Rotation(new Vector3f(8F, 8F, 8F), x, y, z, false);
    }

    private static Map<Key, JsonObject> loadDefinitions(Iterable<Path> artifacts)
            throws IOException {
        Map<Key, JsonObject> result = new LinkedHashMap<>();
        for (Path artifact : artifacts) {
            try (ZipFile zip = new ZipFile(artifact.toFile())) {
                var entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (entry.isDirectory() || !name.startsWith("assets/")
                            || !name.contains("/models/block/") || !name.endsWith(".json")) {
                        continue;
                    }
                    JsonObject json;
                    try (InputStream input = zip.getInputStream(entry);
                         InputStreamReader reader = new InputStreamReader(
                                 input, StandardCharsets.UTF_8)) {
                        JsonElement parsed = JsonParser.parseReader(reader);
                        if (!parsed.isJsonObject()) {
                            continue;
                        }
                        json = parsed.getAsJsonObject();
                    }
                    if (!LOADER.equals(string(json.get("loader")))) {
                        continue;
                    }
                    String[] parts = name.split("/", 4);
                    if (parts.length != 4 || !parts[2].equals("models")) {
                        continue;
                    }
                    String value = parts[3].substring(0, parts[3].length() - 5);
                    result.put(Key.parse(parts[1] + ':' + value), json);
                }
            }
        }
        return result;
    }

    private static JsonObject object(JsonElement element) {
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private static String string(JsonElement element) {
        return element != null && element.isJsonPrimitive()
                && element.getAsJsonPrimitive().isString() ? element.getAsString() : null;
    }

    /** Flattening summary and the exact runtime texture keys added by the models. */
    public record Result(int modelCount, Set<Key> textures) {
    }
}
