/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.mekanism.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;

/** Resource-pack extension factory registered before resource loading. */
final class MekanismResourceExtensionType
        implements ResourcePack.Extension<MekanismResourceExtension> {

    private static final Key KEY = Key.parse("bluemap_mekanism:prototype");

    private final BlockRendererType renderer;
    private final MekanismRuntime runtime;

    MekanismResourceExtensionType(BlockRendererType renderer, MekanismRuntime runtime) {
        this.renderer = renderer;
        this.runtime = runtime;
    }

    @Override
    public Key getKey() {
        return KEY;
    }

    @Override
    public MekanismResourceExtension create(ResourcePack pack) {
        return new MekanismResourceExtension(pack, renderer, runtime);
    }
}
