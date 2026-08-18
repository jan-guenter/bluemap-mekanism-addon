/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.mekanism.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Renders persisted transmitter topology or an opaque whole-block cover. */
final class MekanismRenderer implements BlockRenderer {

    private static final ThreadLocal<Boolean> STOCK_FALLBACK =
            ThreadLocal.withInitial(() -> Boolean.FALSE);

    private final ResourcePack resourcePack;
    private final TextureGallery textures;
    private final RenderSettings settings;
    private final MekanismRuntime runtime;
    private final MekanismRuntime.PackData data;
    private final TransmitterEmitter emitter;
    private final EnergyCubeEmitter energyCubeEmitter;
    private final ConnectedGlassEmitter connectedGlassEmitter;
    private final Map<BlockRendererType, BlockRenderer> hosts = new IdentityHashMap<>();

    MekanismRenderer(
            ResourcePack resourcePack,
            TextureGallery textures,
            RenderSettings settings,
            MekanismRuntime runtime,
            MekanismRuntime.PackData data
    ) {
        this.resourcePack = resourcePack;
        this.textures = textures;
        this.settings = settings;
        this.runtime = runtime;
        this.data = data;
        this.emitter = new TransmitterEmitter(resourcePack, textures, settings);
        this.energyCubeEmitter = new EnergyCubeEmitter(resourcePack, textures);
        this.connectedGlassEmitter = new ConnectedGlassEmitter(resourcePack, textures);
    }

    @Override
    public void render(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        int start = target.getStart();
        try {
            if (!renderMekanism(block, variant, target, mapColor)) {
                stock(block, variant, target, mapColor);
            }
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (Error error) {
            MekanismRuntime.throwIfFatal(error);
            reset(target, start);
            runtime.inactive("renderer-" + error.getClass().getSimpleName());
            stockSafely(block, variant, target, mapColor, start);
        } catch (RuntimeException exception) {
            reset(target, start);
            runtime.report("renderer-" + exception.getClass().getSimpleName());
            stockSafely(block, variant, target, mapColor, start);
        }
    }

    private boolean renderMekanism(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        if (!runtime.active() || data == null) {
            return false;
        }
        String blockId = block.getBlockState().getId().getFormatted();
        if (MekanismCatalog.CONNECTED_GLASS.contains(blockId)) {
            return connectedGlassEmitter.emit(blockId, variant, block, target, mapColor);
        }
        if (MekanismCatalog.ENERGY_CUBES.contains(blockId)) {
            EnergyCubeBlockEntityData entity =
                    block.getBlockEntity() instanceof EnergyCubeBlockEntityData found
                            ? found : null;
            return energyCubeEmitter.emit(data.energyCube(), variant, entity,
                    block, target, mapColor);
        }
        if (!MekanismCatalog.TRANSMITTERS.contains(blockId)) {
            return false;
        }
        TransmitterBlockEntityData entity =
                block.getBlockEntity() instanceof TransmitterBlockEntityData found
                        ? found : null;
        boolean emitted = emitter.emit(blockId, variant, entity, data, block, target, mapColor);
        if (!emitted) {
            return false;
        }
        if (entity != null && entity.coverState() != null) {
            renderCover(entity.coverState(), block, target, mapColor);
        }
        return true;
    }

    private boolean renderCover(
            String serialized,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        BlockState cover;
        try {
            cover = BlockState.fromString(serialized);
        } catch (IllegalArgumentException exception) {
            return false;
        }
        var state = resourcePack.getBlockStates().get(cover.getId());
        if (state == null || MekanismCatalog.TRANSMITTERS.contains(
                cover.getId().getFormatted())) {
            return false;
        }
        List<Variant> variants = new ArrayList<>();
        state.forEach(cover, block.getX(), block.getY(), block.getZ(), variants::add);
        if (variants.isEmpty()) {
            return false;
        }
        for (Variant coverVariant : variants) {
            BlockRendererType type = coverVariant.getRenderer();
            if (type == null || type.getKey().getFormatted().equals(
                    "bluemap_mekanism:transmitter")) {
                return false;
            }
            hosts.computeIfAbsent(
                    type, found -> found.create(resourcePack, textures, settings)
            ).render(block, coverVariant, target, mapColor);
        }
        return true;
    }

    private void stock(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        if (STOCK_FALLBACK.get()) {
            return;
        }
        STOCK_FALLBACK.set(Boolean.TRUE);
        try {
            BlockRendererType type = data == null
                    ? BlockRendererType.DEFAULT : data.variants().original(variant);
            hosts.computeIfAbsent(
                    type, found -> found.create(resourcePack, textures, settings)
            ).render(block, variant, target, mapColor);
        } finally {
            STOCK_FALLBACK.set(Boolean.FALSE);
        }
    }

    private void stockSafely(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor,
            int start
    ) {
        try {
            stock(block, variant, target, mapColor);
        } catch (Error error) {
            MekanismRuntime.throwIfFatal(error);
            reset(target, start);
            runtime.report("stock-fallback-" + error.getClass().getSimpleName());
        } catch (RuntimeException exception) {
            reset(target, start);
            runtime.report("stock-fallback-" + exception.getClass().getSimpleName());
        }
    }

    private static void reset(TileModelView target, int start) {
        target.getTileModel().reset(start);
        target.initialize(start);
    }
}
