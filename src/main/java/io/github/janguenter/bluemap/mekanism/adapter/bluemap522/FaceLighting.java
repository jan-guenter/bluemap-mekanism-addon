/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.mekanism.adapter.bluemap522;

import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.math.VectorM3f;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

/** Samples light on one model face. */
final class FaceLighting {

    private FaceLighting() {
    }

    static Sample sample(BlockNeighborhood block, Direction direction) {
        return sample(block, direction, null);
    }

    static Sample sample(BlockNeighborhood block, Direction direction, Variant transform) {
        LightData own = block.getLightData();
        VectorM3f vector = new VectorM3f(0F, 0F, 0F).set(direction.toVector());
        if (transform != null && transform.isTransformed()) {
            vector.rotateAndScale(transform.getTransformMatrix());
        }
        LightData faced = block.getNeighborBlock(
                Math.round(vector.x), Math.round(vector.y), Math.round(vector.z)
        ).getLightData();
        return new Sample(
                Math.max(own.getSkyLight(), faced.getSkyLight()),
                Math.max(own.getBlockLight(), faced.getBlockLight())
        );
    }

    record Sample(int sunlight, int blocklight) {
    }
}
