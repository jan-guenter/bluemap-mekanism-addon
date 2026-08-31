/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.mekanism.adapter.bluemap523;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.ResourceExtensionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdapterBoundaryTest {

    @Test
    void usesSharedAdapterHelpersWithoutLocalCopies() {
        ResourcePack.Extension<MekanismResourceExtension> extension =
                BlueMap523Adapter.extension();

        assertInstanceOf(ResourceExtensionType.class, extension);
        assertEquals(Key.parse("bluemap_mekanism:prototype"), extension.getKey());
        assertInstanceOf(MekanismResourceExtension.class, extension.create(null));
        assertThrows(ClassNotFoundException.class, () -> Class.forName(
                "io.github.janguenter.bluemap.mekanism.adapter.bluemap523."
                        + "MekanismResourceExtensionType"
        ));
        assertThrows(ClassNotFoundException.class, () -> Class.forName(
                "io.github.janguenter.bluemap.mekanism.adapter.bluemap523."
                        + "AdapterCompatibility"
        ));
    }
}
