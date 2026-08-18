/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.mekanism.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.mekanism.model.CompositeModelFlattener;
import io.github.janguenter.bluemap.mekanism.model.ObjModel;
import io.github.janguenter.bluemap.mekanism.model.ObjModelParser;
import io.github.janguenter.bluemap.mekanism.model.EnergyCubeModel;
import io.github.janguenter.bluemap.mekanism.profile.ExactMekanismArtifactDetector;
import io.github.janguenter.bluemap.mekanism.profile.ExactMekanismArtifactDetector.Bundle;
import io.github.janguenter.bluemap.mekanism.profile.ExactMekanismArtifactDetector.Role;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Exact-artifact composite/OBJ compilation and transmitter renderer wrapping. */
final class MekanismResourceExtension implements ResourcePackExtension {

    private static final String SMALL_OBJ =
            "assets/mekanism/models/block/transmitter_small.obj.mek";
    private static final String LARGE_OBJ =
            "assets/mekanism/models/block/transmitter_large.obj.mek";
    private static final String SMALL_MTL =
            "assets/mekanism/models/block/transmitter_small.mtl";
    private static final String LARGE_MTL =
            "assets/mekanism/models/block/transmitter_large.mtl";
    private static final String GLASS_MTL =
            "assets/mekanism/models/block/transmitter_glass_large.mtl";
    private static final String ENERGY_CUBE_MODEL =
            "assets/mekanism/models/block/energy_cube/base.json";

    private final ResourcePack resourcePack;
    private final BlockRendererType renderer;
    private final MekanismRuntime runtime;

    private Set<Key> compositeTextures = Set.of();
    private ObjModel small;
    private ObjModel large;
    private Map<String, String> smallMaterials = Map.of();
    private Map<String, String> largeMaterials = Map.of();
    private Map<String, String> glassMaterials = Map.of();
    private EnergyCubeModel energyCube;
    private int compositeCount;

    MekanismResourceExtension(
            ResourcePack resourcePack,
            BlockRendererType renderer,
            MekanismRuntime runtime
    ) {
        this.resourcePack = resourcePack;
        this.renderer = renderer;
        this.runtime = runtime;
    }

    @Override
    public void loadResources(Iterable<Path> roots) {
        if (Boolean.getBoolean("bluemap.mekanism.disabled")) {
            runtime.inactive("operator-disabled");
            return;
        }
        Bundle artifacts = ExactMekanismArtifactDetector.find(roots).orElse(null);
        if (artifacts == null) {
            runtime.inactive("exact-family-artifacts-not-found");
            return;
        }
        try {
            CompositeModelFlattener.Result flattened = CompositeModelFlattener.flatten(
                    resourcePack,
                    List.of(artifacts.path(Role.CORE), artifacts.path(Role.GENERATORS),
                            artifacts.path(Role.MORE_MACHINE))
            );
            compositeCount = flattened.modelCount();
            compositeTextures = flattened.textures();
            if (compositeCount != 359) {
                throw new IOException("composite roster changed: " + compositeCount);
            }
            try (ZipFile core = new ZipFile(artifacts.path(Role.CORE).toFile())) {
                small = ObjModelParser.parse(read(core, SMALL_OBJ));
                large = ObjModelParser.parse(read(core, LARGE_OBJ));
                smallMaterials = ObjModelParser.parseMaterials(read(core, SMALL_MTL));
                largeMaterials = ObjModelParser.parseMaterials(read(core, LARGE_MTL));
                glassMaterials = ObjModelParser.parseMaterials(read(core, GLASS_MTL));
                energyCube = EnergyCubeModel.compile(read(core, ENERGY_CUBE_MODEL));
            }
            LinkedHashSet<Key> usedTextures = new LinkedHashSet<>(compositeTextures);
            collectTransmitterTextures(usedTextures);
            collectEnergyCubeTextures(usedTextures);
            usedTextures.addAll(ConnectedGlassEmitter.usedTextureKeys());
            compositeTextures = Set.copyOf(usedTextures);
            runtime.activate();
        } catch (IOException | RuntimeException exception) {
            compositeTextures = Set.of();
            runtime.inactive("resource-compile-" + exception.getClass().getSimpleName()
                    + '-' + safeMessage(exception));
        }
    }

    @Override
    public Set<Key> collectUsedTextureKeys() {
        return compositeTextures;
    }

    @Override
    public void bake() {
        if (!runtime.active() || small == null || large == null || energyCube == null) {
            return;
        }
        VariantRendererCatalog variants = VariantRendererCatalog.wrap(resourcePack, renderer);
        MekanismRuntime.PackData data = new MekanismRuntime.PackData(
                small, large, smallMaterials, largeMaterials, glassMaterials,
                energyCube, variants, compositeCount
        );
        runtime.install(resourcePack, data);
        System.out.println("BlueMap Mekanism add-on active: flattened " + compositeCount
                + " composite models for 185 blocks; compiled 2 transmitter OBJs; wrapped "
                + variants.size() + " transmitter/energy-cube variants with Covers support.");
    }

    private static byte[] read(ZipFile zip, String path) throws IOException {
        ZipEntry entry = zip.getEntry(path);
        if (entry == null || entry.isDirectory()) {
            throw new IOException("missing installed resource " + path);
        }
        try (InputStream input = zip.getInputStream(entry)) {
            return input.readAllBytes();
        }
    }

    private void collectTransmitterTextures(Set<Key> target) throws IOException {
        for (String blockId : MekanismCatalog.TRANSMITTERS) {
            var state = resourcePack.getBlockStates().get(Key.parse(blockId));
            if (state == null) {
                throw new IOException("missing transmitter blockstate " + blockId);
            }
            state.forEach(variant -> {
                var model = variant.getModel().getResource(resourcePack.getModels()::get);
                if (model == null) {
                    return;
                }
                model.applyParent(resourcePack.getModels());
                model.getTextures().values().forEach(variable -> {
                    var path = variable.getTexturePath(model.getTextures()::get);
                    if (path != null) {
                        target.add(path);
                    }
                });
            });
        }
    }

    private void collectEnergyCubeTextures(Set<Key> target) throws IOException {
        for (String blockId : MekanismCatalog.ENERGY_CUBES) {
            var state = resourcePack.getBlockStates().get(Key.parse(blockId));
            if (state == null) {
                throw new IOException("missing energy cube blockstate " + blockId);
            }
            state.forEach(variant -> {
                var model = variant.getModel().getResource(resourcePack.getModels()::get);
                if (model == null) {
                    return;
                }
                model.applyParent(resourcePack.getModels());
                model.getTextures().values().forEach(variable -> {
                    var path = variable.getTexturePath(model.getTextures()::get);
                    if (path != null) {
                        target.add(path);
                    }
                });
            });
        }
    }

    private static String safeMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null) {
            return "unknown";
        }
        return message.replaceAll("[^A-Za-z0-9_.:-]+", "_");
    }
}
