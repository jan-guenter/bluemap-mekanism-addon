/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.mekanism.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.mca.blockentity.BlockEntityType;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.RegistryGuard;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.ResourceExtensionType;

import java.util.ArrayList;
import java.util.List;

/** BlueMap 5.23 feature-backport registration boundary. */
public final class BlueMap523Adapter {

    private static final MekanismRuntime RUNTIME = MekanismRuntime.INSTANCE;
    private static final BlockRendererType RENDERER = new BlockRendererType.Impl(
            Key.parse("bluemap_mekanism:transmitter"), BlueMap523Adapter::createRenderer
    );
    private static final Key EXTENSION_KEY = Key.parse("bluemap_mekanism:prototype");
    private static final ResourcePack.Extension<MekanismResourceExtension> EXTENSION =
            new ResourceExtensionType<>(
                    EXTENSION_KEY,
                    pack -> new MekanismResourceExtension(pack, RENDERER, RUNTIME)
            );
    private static final List<BlockEntityType> BLOCK_ENTITIES = createBlockEntityTypes();

    private BlueMap523Adapter() {
    }

    public static synchronized boolean install() {
        if (!RegistryGuard.canRegister(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.canRegister(ResourcePack.Extension.REGISTRY, EXTENSION)) {
            RUNTIME.inactive("registry-collision");
            return false;
        }
        for (BlockEntityType type : BLOCK_ENTITIES) {
            if (!RegistryGuard.canRegister(BlockEntityType.REGISTRY, type)) {
                RUNTIME.inactive("block-entity-registry-collision");
                return false;
            }
        }
        if (!RegistryGuard.register(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.register(ResourcePack.Extension.REGISTRY, EXTENSION)) {
            return false;
        }
        for (BlockEntityType type : BLOCK_ENTITIES) {
            if (!RegistryGuard.register(BlockEntityType.REGISTRY, type)) {
                return false;
            }
        }
        return true;
    }

    static ResourcePack.Extension<MekanismResourceExtension> extension() {
        return EXTENSION;
    }

    private static BlockRenderer createRenderer(
            ResourcePack pack,
            TextureGallery gallery,
            RenderSettings settings
    ) {
        try {
            return new MekanismRenderer(pack, gallery, settings, RUNTIME, RUNTIME.data(pack));
        } catch (Error error) {
            MekanismRuntime.throwIfFatal(error);
            RUNTIME.inactive("renderer-construction-" + error.getClass().getSimpleName());
            return BlockRendererType.DEFAULT.create(pack, gallery, settings);
        } catch (RuntimeException exception) {
            RUNTIME.inactive("renderer-construction-" + exception.getClass().getSimpleName());
            return BlockRendererType.DEFAULT.create(pack, gallery, settings);
        }
    }

    private static List<BlockEntityType> createBlockEntityTypes() {
        List<BlockEntityType> result = new ArrayList<>();
        for (String id : MekanismCatalog.TRANSMITTERS) {
            result.add(new BlockEntityType.Impl(Key.parse(id), TransmitterBlockEntityData.class));
        }
        for (String id : MekanismCatalog.ENERGY_CUBES) {
            result.add(new BlockEntityType.Impl(Key.parse(id), EnergyCubeBlockEntityData.class));
        }
        return List.copyOf(result);
    }
}
