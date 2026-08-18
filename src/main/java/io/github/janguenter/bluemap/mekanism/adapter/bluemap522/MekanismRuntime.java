/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.mekanism.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import io.github.janguenter.bluemap.mekanism.model.ObjModel;
import io.github.janguenter.bluemap.mekanism.model.EnergyCubeModel;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** Shared activation, installed-resource and bounded-diagnostic state. */
final class MekanismRuntime {

    static final MekanismRuntime INSTANCE = new MekanismRuntime();

    private final AtomicBoolean active = new AtomicBoolean();
    private final AtomicInteger diagnostics = new AtomicInteger();
    private final Map<ResourcePack, PackData> packs = new WeakHashMap<>();

    private MekanismRuntime() {
    }

    boolean active() {
        return active.get();
    }

    void activate() {
        active.set(true);
    }

    void inactive(String reason) {
        active.set(false);
        report("inactive-" + reason);
    }

    synchronized void install(ResourcePack pack, PackData data) {
        packs.put(pack, data);
    }

    synchronized PackData data(ResourcePack pack) {
        return packs.get(pack);
    }

    void report(String reason) {
        if (diagnostics.incrementAndGet() <= 16) {
            System.err.println("BlueMap Mekanism add-on: " + reason + '.');
        }
    }

    @SuppressWarnings("removal")
    static void throwIfFatal(Error error) {
        if (error instanceof OutOfMemoryError outOfMemory) {
            throw outOfMemory;
        }
        if (error instanceof ThreadDeath threadDeath) {
            throw threadDeath;
        }
    }

    record PackData(
            ObjModel small,
            ObjModel large,
            Map<String, String> smallMaterials,
            Map<String, String> largeMaterials,
            Map<String, String> glassMaterials,
            EnergyCubeModel energyCube,
            VariantRendererCatalog variants,
            int composites
    ) {
        PackData {
            smallMaterials = Map.copyOf(smallMaterials);
            largeMaterials = Map.copyOf(largeMaterials);
            glassMaterials = Map.copyOf(glassMaterials);
        }
    }
}
