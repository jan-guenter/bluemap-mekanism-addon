/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.mekanism.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.Key;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/** Exact original renderer identity for each wrapped transmitter variant. */
final class VariantRendererCatalog {

    private final Map<Variant, BlockRendererType> originals;

    private VariantRendererCatalog(Map<Variant, BlockRendererType> originals) {
        this.originals = Collections.unmodifiableMap(originals);
    }

    static VariantRendererCatalog wrap(ResourcePack pack, BlockRendererType wrapper) {
        IdentityHashMap<Variant, BlockRendererType> originals = new IdentityHashMap<>();
        MekanismCatalog.CUSTOM_BLOCKS.forEach(id -> {
            var state = pack.getBlockStates().get(Key.parse(id));
            if (state != null) {
                state.forEach(variant -> {
                    if (variant.getRenderer() != wrapper) {
                        originals.put(variant, variant.getRenderer());
                        variant.setRenderer(wrapper);
                    }
                });
            }
        });
        return new VariantRendererCatalog(originals);
    }

    BlockRendererType original(Variant variant) {
        return originals.getOrDefault(variant, BlockRendererType.DEFAULT);
    }

    int size() {
        return originals.size();
    }
}
