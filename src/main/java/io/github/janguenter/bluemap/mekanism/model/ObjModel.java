/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.mekanism.model;

import java.util.List;
import java.util.Map;

/** Minimal immutable OBJ geometry grouped by Mekanism's named connection parts. */
public record ObjModel(Map<String, List<Triangle>> groups) {

    public ObjModel {
        groups = Map.copyOf(groups);
    }

    /** One textured triangle and its source material name. */
    public record Triangle(Vertex first, Vertex second, Vertex third, String material) {
    }

    /** OBJ position plus normalized UV. */
    public record Vertex(float x, float y, float z, float u, float v) {
    }
}
