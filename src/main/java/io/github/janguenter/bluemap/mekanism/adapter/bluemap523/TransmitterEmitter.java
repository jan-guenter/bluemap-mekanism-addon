/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.mekanism.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.TextureVariable;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.mekanism.model.ObjModel;
import io.github.janguenter.bluemap.mekanism.model.ObjModel.Triangle;
import io.github.janguenter.bluemap.mekanism.model.ObjModel.Vertex;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Emits the six selected connection objects from the installed transmitter OBJ. */
final class TransmitterEmitter {

    private static final String[] SIDES = {"down", "up", "north", "south", "west", "east"};
    private static final String[] MODES = {"NORMAL", "PUSH", "PULL", "NONE"};

    private final ResourcePack resourcePack;
    private final TextureGallery textures;
    private final RenderSettings settings;
    private final Map<Model, Map<String, Material>> normalCache = new IdentityHashMap<>();
    private final Map<Model, Map<String, Material>> glassCache = new IdentityHashMap<>();

    TransmitterEmitter(
            ResourcePack resourcePack,
            TextureGallery textures,
            RenderSettings settings
    ) {
        this.resourcePack = resourcePack;
        this.textures = textures;
        this.settings = settings;
    }

    boolean emit(
            String blockId,
            Variant variant,
            TransmitterBlockEntityData entity,
            MekanismRuntime.PackData data,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        Model textureModel = variant.getModel().getResource(resourcePack.getModels()::get);
        if (textureModel == null) {
            return false;
        }
        boolean large = MekanismCatalog.LARGE.contains(blockId);
        ObjModel obj = large ? data.large() : data.small();
        Map<String, String> materialVariables = large
                ? data.largeMaterials() : data.smallMaterials();
        Map<String, Material> materials = normalCache.computeIfAbsent(
                textureModel, ignored -> resolve(textureModel, materialVariables)
        );
        if (materials.isEmpty()) {
            return false;
        }

        int transmitterMask = entity == null ? 0 : entity.transmitterConnections();
        int acceptorMask = entity == null ? 0 : entity.acceptorConnections();
        List<List<Triangle>> groups = new ArrayList<>(6);
        for (int side = 0; side < SIDES.length; side++) {
            int bit = 1 << side;
            int mode;
            if (((transmitterMask | acceptorMask) & bit) == 0) {
                mode = 3;
            } else if ((transmitterMask & bit) != 0) {
                mode = 0;
            } else {
                mode = entity == null ? 0 : entity.connectionMode(side);
            }
            List<Triangle> selected = obj.groups().get(SIDES[side] + MODES[mode]);
            if (selected == null || !preflight(selected, materials)) {
                return false;
            }
            groups.add(selected);
        }

        for (List<Triangle> group : groups) {
            emitGroup(group, materials, block, target, mapColor);
        }

        if (entity != null && entity.hasColor() && MekanismCatalog.GLASS.contains(blockId)) {
            Map<String, Material> glass = glassCache.computeIfAbsent(
                    textureModel, ignored -> resolve(textureModel, data.glassMaterials())
            );
            if (!glass.isEmpty() && groups.stream().allMatch(group -> preflight(group, glass))) {
                for (List<Triangle> group : groups) {
                    emitGroup(group, glass, block, target, mapColor);
                }
            }
        }
        if (mapColor.a > 0F) {
            mapColor.flatten().straight();
        }
        return true;
    }

    private Map<String, Material> resolve(Model model, Map<String, String> mappings) {
        Map<String, Material> result = new java.util.HashMap<>();
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            TextureVariable variable = model.getTextures().get(entry.getValue());
            ResourcePath<Texture> path = variable == null
                    ? null : variable.getTexturePath(model.getTextures()::get);
            Texture texture = path == null ? null : resourcePack.getTextures().get(path);
            if (path != null && texture != null) {
                result.put(entry.getKey(), new Material(textures.get(path), texture));
            }
        }
        return Map.copyOf(result);
    }

    private static boolean preflight(List<Triangle> triangles, Map<String, Material> materials) {
        for (Triangle triangle : triangles) {
            if (!materials.containsKey(triangle.material())) {
                return false;
            }
        }
        return true;
    }

    private void emitGroup(
            List<Triangle> triangles,
            Map<String, Material> materials,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        for (Triangle triangle : triangles) {
            Direction direction = nearestDirection(triangle);
            if (settings.isRenderTopOnly() && direction != Direction.UP) {
                continue;
            }
            FaceLighting.Sample light = FaceLighting.sample(block, direction);
            int visibleLight = settings.isCaveDetectionUsesBlockLight()
                    ? Math.max(light.sunlight(), light.blocklight()) : light.sunlight();
            if (block.isRemoveIfCave() && visibleLight == 0) {
                continue;
            }
            Material material = materials.get(triangle.material());
            int index = target.add(1);
            TileModel mesh = target.getTileModel();
            positions(mesh, index, triangle.first(), triangle.second(), triangle.third());
            uvs(mesh, index, triangle.first(), triangle.second(), triangle.third());
            mesh.setMaterialIndex(index, material.index());
            mesh.setColor(index, 1F, 1F, 1F);
            mesh.setAOs(index, 1F, 1F, 1F);
            mesh.setSunlight(index, light.sunlight());
            mesh.setBlocklight(index, light.blocklight());
            if (direction == Direction.UP) {
                Color average = new Color().set(material.texture().getColorPremultiplied());
                float lightFactor = Math.max(light.sunlight(), light.blocklight()) / 15F;
                lightFactor = (1F - settings.getAmbientLight()) * lightFactor
                        + settings.getAmbientLight();
                average.r *= lightFactor;
                average.g *= lightFactor;
                average.b *= lightFactor;
                mapColor.add(average);
            }
        }
    }

    private static Direction nearestDirection(Triangle triangle) {
        Vertex a = triangle.first();
        Vertex b = triangle.second();
        Vertex c = triangle.third();
        float abx = b.x() - a.x();
        float aby = b.y() - a.y();
        float abz = b.z() - a.z();
        float acx = c.x() - a.x();
        float acy = c.y() - a.y();
        float acz = c.z() - a.z();
        float x = aby * acz - abz * acy;
        float y = abz * acx - abx * acz;
        float z = abx * acy - aby * acx;
        float ax = Math.abs(x);
        float ay = Math.abs(y);
        float az = Math.abs(z);
        if (ay >= ax && ay >= az) {
            return y >= 0 ? Direction.UP : Direction.DOWN;
        }
        if (ax >= az) {
            return x >= 0 ? Direction.EAST : Direction.WEST;
        }
        return z >= 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private static void positions(TileModel mesh, int index, Vertex a, Vertex b, Vertex c) {
        mesh.setPositions(index, a.x(), a.y(), a.z(), b.x(), b.y(), b.z(),
                c.x(), c.y(), c.z());
    }

    private static void uvs(TileModel mesh, int index, Vertex a, Vertex b, Vertex c) {
        mesh.setUvs(index, a.u(), a.v(), b.u(), b.v(), c.u(), c.v());
    }

    private record Material(int index, Texture texture) {
    }
}
