/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.mekanism.adapter.bluemap522;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;
import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.util.math.MatrixM4f;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.mekanism.model.EnergyCubeModel;

import java.util.List;

/** Emits stable energy-cube frame/LED geometry and persisted configured ports. */
final class EnergyCubeEmitter {

    private static final float BLOCK_SCALE = 1F / 16F;
    private static final String[] SIDES = {"front", "left", "right", "back", "top", "bottom"};

    private final ResourcePack resourcePack;
    private final TextureGallery textureGallery;

    EnergyCubeEmitter(ResourcePack resourcePack, TextureGallery textureGallery) {
        this.resourcePack = resourcePack;
        this.textureGallery = textureGallery;
    }

    boolean emit(
            EnergyCubeModel geometry,
            Variant variant,
            EnergyCubeBlockEntityData entity,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        Model textureModel = variant.getModel().getResource(resourcePack.getModels()::get);
        if (textureModel == null || !preflight(geometry, textureModel, entity)) {
            return false;
        }
        int modelStart = target.getTileModel().size();
        emitGroup(geometry.groups().get("frame"), textureModel, variant, block, target, mapColor);
        for (int side = 0; side < SIDES.length; side++) {
            emitGroup(geometry.groups().get(SIDES[side] + "LEDs"), textureModel,
                    variant, block, target, mapColor);
            if (entity != null && entity.hasPort(side)) {
                emitGroup(geometry.groups().get(SIDES[side] + "Port"), textureModel,
                        variant, block, target, mapColor);
            }
        }
        int count = target.getTileModel().size() - modelStart;
        if (count > 0 && variant.isTransformed()) {
            target.initialize(modelStart).transform(variant.getTransformMatrix());
        }
        target.initialize(modelStart);
        if (mapColor.a > 0F) {
            mapColor.flatten().straight();
        }
        return count > 0;
    }

    private boolean preflight(
            EnergyCubeModel geometry,
            Model textureModel,
            EnergyCubeBlockEntityData entity
    ) {
        for (String name : geometry.groups().keySet()) {
            if (name.endsWith("Port") && (entity == null
                    || !entity.hasPort(relativeSide(name)))) {
                continue;
            }
            for (Element element : geometry.groups().get(name)) {
                for (Face face : element.getFaces().values()) {
                    ResourcePath<Texture> path = face.getTexture()
                            .getTexturePath(textureModel.getTextures()::get);
                    if (path == null || resourcePack.getTextures().get(path) == null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static int relativeSide(String group) {
        for (int side = 0; side < SIDES.length; side++) {
            if (group.startsWith(SIDES[side])) {
                return side;
            }
        }
        return -1;
    }

    private void emitGroup(
            List<Element> elements,
            Model model,
            Variant variant,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        for (Element element : elements) {
            int elementStart = target.getTileModel().size();
            emitElement(model, element, variant, block, target, mapColor);
            int elementCount = target.getTileModel().size() - elementStart;
            if (elementCount > 0) {
                target.initialize(elementStart);
                target.transform(new MatrixM4f()
                        .copy(element.getRotation().getMatrix())
                        .scale(BLOCK_SCALE, BLOCK_SCALE, BLOCK_SCALE));
            }
        }
    }

    private void emitElement(
            Model model,
            Element element,
            Variant variant,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        Vector3f from = element.getFrom();
        Vector3f to = element.getTo();
        float x0 = from.getX();
        float y0 = from.getY();
        float z0 = from.getZ();
        float x1 = to.getX();
        float y1 = to.getY();
        float z1 = to.getZ();
        emitFace(model, element, Direction.DOWN, variant, block, target, mapColor,
                x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        emitFace(model, element, Direction.UP, variant, block, target, mapColor,
                x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0);
        emitFace(model, element, Direction.NORTH, variant, block, target, mapColor,
                x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0);
        emitFace(model, element, Direction.SOUTH, variant, block, target, mapColor,
                x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
        emitFace(model, element, Direction.WEST, variant, block, target, mapColor,
                x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
        emitFace(model, element, Direction.EAST, variant, block, target, mapColor,
                x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1);
    }

    private void emitFace(
            Model model,
            Element element,
            Direction direction,
            Variant variant,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor,
            float ax, float ay, float az,
            float bx, float by, float bz,
            float cx, float cy, float cz,
            float dx, float dy, float dz
    ) {
        Face face = element.getFaces().get(direction);
        if (face == null) {
            return;
        }
        ResourcePath<Texture> textureKey = face.getTexture()
                .getTexturePath(model.getTextures()::get);
        Texture texture = textureKey == null ? null : resourcePack.getTextures().get(textureKey);
        if (textureKey == null || texture == null) {
            return;
        }
        int start = target.add(2);
        TileModel mesh = target.getTileModel();
        mesh.setPositions(start, ax, ay, az, bx, by, bz, cx, cy, cz);
        mesh.setPositions(start + 1, ax, ay, az, cx, cy, cz, dx, dy, dz);

        Vector4f raw = face.getUv();
        float u0 = raw.getX() / 16F;
        float v0 = raw.getY() / 16F;
        float u1 = raw.getZ() / 16F;
        float v1 = raw.getW() / 16F;
        float[][] corners = {{u0, v1}, {u1, v1}, {u1, v0}, {u0, v0}};
        int rotation = Math.floorMod(face.getRotation() / 90, 4);
        float[] uv0 = corners[rotation];
        float[] uv1 = corners[(rotation + 1) % 4];
        float[] uv2 = corners[(rotation + 2) % 4];
        float[] uv3 = corners[(rotation + 3) % 4];
        mesh.setUvs(start, uv0[0], uv0[1], uv1[0], uv1[1], uv2[0], uv2[1]);
        mesh.setUvs(start + 1, uv0[0], uv0[1], uv2[0], uv2[1], uv3[0], uv3[1]);

        int material = textureGallery.get(textureKey);
        FaceLighting.Sample light = FaceLighting.sample(block, direction, variant);
        for (int index = start; index < start + 2; index++) {
            mesh.setMaterialIndex(index, material);
            mesh.setColor(index, 1F, 1F, 1F);
            mesh.setAOs(index, 1F, 1F, 1F);
            mesh.setSunlight(index, light.sunlight());
            mesh.setBlocklight(index, light.blocklight());
        }
        if (direction == Direction.UP) {
            mapColor.add(new Color().set(texture.getColorPremultiplied()));
        }
    }
}
