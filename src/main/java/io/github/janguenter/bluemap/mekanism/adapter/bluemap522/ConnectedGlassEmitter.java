/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.mekanism.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
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

import java.util.Set;

/** Emits cube faces while suppressing exact formed-glass neighbor seams. */
final class ConnectedGlassEmitter {

    private static final String STRUCTURAL = "mekanism:structural_glass";
    private static final String REACTOR = "mekanismgenerators:reactor_glass";
    private static final String LASER = "mekanismgenerators:laser_focus_matrix";
    private static final Key STRUCTURAL_SHEET =
            Key.parse("mekanism:ctm/structural_glass");
    private static final Key REACTOR_SHEET =
            Key.parse("mekanismgenerators:ctm/reactor_glass");
    private static final Key LASER_SHEET =
            Key.parse("mekanismgenerators:ctm/laser_focus_matrix");
    private static final Set<Key> CTM_TEXTURES = Set.of(
            STRUCTURAL_SHEET, REACTOR_SHEET, LASER_SHEET
    );
    private static final int[] INITIAL_CELLS = {18, 19, 17, 16};
    private static final int[] BASE_CELLS = {4, 5, 1, 0};
    private static final float[][] QUADRANT_BOUNDS = {
            {0F, 0F, 0.5F, 0.5F},
            {0.5F, 0F, 1F, 0.5F},
            {0.5F, 0.5F, 1F, 1F},
            {0F, 0.5F, 0.5F, 1F}
    };

    private final ResourcePack resourcePack;
    private final TextureGallery textures;

    ConnectedGlassEmitter(ResourcePack resourcePack, TextureGallery textures) {
        this.resourcePack = resourcePack;
        this.textures = textures;
    }

    static Set<Key> usedTextureKeys() {
        return CTM_TEXTURES;
    }

    boolean emit(
            String blockId,
            Variant variant,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        Model model = variant.getModel().getResource(resourcePack.getModels()::get);
        TextureVariable variable = model == null ? null : model.getTextures().get("all");
        ResourcePath<Texture> key = variable == null
                ? null : variable.getTexturePath(model.getTextures()::get);
        Texture texture = key == null ? null : resourcePack.getTextures().get(key);
        Key sheetKey = sheetKey(blockId);
        Texture sheet = sheetKey == null ? null : resourcePack.getTextures().get(sheetKey);
        if (key == null || texture == null || sheet == null) {
            return false;
        }
        int baseMaterial = textures.get(key);
        int sheetMaterial = textures.get(sheetKey);
        for (Direction direction : Direction.values()) {
            var offset = direction.toVector();
            String neighbor = block.getNeighborBlock(
                    Math.round(offset.getX()), Math.round(offset.getY()),
                    Math.round(offset.getZ())
            ).getBlockState().getId().getFormatted();
            if (!cullsFace(blockId, neighbor)) {
                emitFace(blockId, direction, baseMaterial, sheetMaterial, block, target);
            }
        }
        mapColor.add(new Color().set(texture.getColorPremultiplied()));
        mapColor.flatten().straight();
        return true;
    }

    private static Key sheetKey(String blockId) {
        return switch (blockId) {
            case STRUCTURAL -> STRUCTURAL_SHEET;
            case REACTOR -> REACTOR_SHEET;
            case LASER -> LASER_SHEET;
            default -> null;
        };
    }

    private static boolean cullsFace(String block, String neighbor) {
        boolean blockGlass = STRUCTURAL.equals(block) || REACTOR.equals(block);
        boolean neighborGlass = STRUCTURAL.equals(neighbor) || REACTOR.equals(neighbor);
        if (blockGlass && neighborGlass) {
            return true;
        }
        return (REACTOR.equals(block) && LASER.equals(neighbor))
                || (LASER.equals(block) && (REACTOR.equals(neighbor)
                || LASER.equals(neighbor)));
    }

    private static void emitFace(
            String blockId,
            Direction direction,
            int baseMaterial,
            int sheetMaterial,
            BlockNeighborhood block,
            TileModelView target
    ) {
        float[][] p = switch (direction) {
            case DOWN -> new float[][]{{0, 0, 0}, {1, 0, 0}, {1, 0, 1}, {0, 0, 1}};
            case UP -> new float[][]{{0, 1, 1}, {1, 1, 1}, {1, 1, 0}, {0, 1, 0}};
            case NORTH -> new float[][]{{1, 0, 0}, {0, 0, 0}, {0, 1, 0}, {1, 1, 0}};
            case SOUTH -> new float[][]{{0, 0, 1}, {1, 0, 1}, {1, 1, 1}, {0, 1, 1}};
            case WEST -> new float[][]{{0, 0, 0}, {0, 0, 1}, {0, 1, 1}, {0, 1, 0}};
            case EAST -> new float[][]{{1, 0, 1}, {1, 0, 0}, {1, 1, 0}, {1, 1, 1}};
        };
        FaceAxes axes = axes(direction);
        Direction bottom = axes.top().getOpposite();
        Direction left = axes.right().getOpposite();
        boolean top = surfaceConnected(blockId, block, direction, axes.top());
        boolean right = surfaceConnected(blockId, block, direction, axes.right());
        boolean bottomConnected = surfaceConnected(blockId, block, direction, bottom);
        boolean leftConnected = surfaceConnected(blockId, block, direction, left);
        boolean bottomLeft = surfaceConnected(
                blockId, block, direction, bottom, left
        );
        boolean bottomRight = surfaceConnected(
                blockId, block, direction, bottom, axes.right()
        );
        boolean topRight = surfaceConnected(
                blockId, block, direction, axes.top(), axes.right()
        );
        boolean topLeft = surfaceConnected(
                blockId, block, direction, axes.top(), left
        );
        int[] cells = {
                selectCell(0, bottomConnected, leftConnected, bottomLeft),
                selectCell(1, bottomConnected, right, bottomRight),
                selectCell(2, top, right, topRight),
                selectCell(3, top, leftConnected, topLeft)
        };
        FaceLighting.Sample light = FaceLighting.sample(block, direction);
        for (int quadrant = 0; quadrant < cells.length; quadrant++) {
            emitQuadrant(p, QUADRANT_BOUNDS[quadrant], cells[quadrant],
                    baseMaterial, sheetMaterial, light, target);
        }
    }

    static int selectCell(
            int quadrant,
            boolean first,
            boolean second,
            boolean diagonal
    ) {
        if (!first && !second) {
            return INITIAL_CELLS[quadrant];
        }
        int base = BASE_CELLS[quadrant];
        if (first && second && diagonal) {
            return base;
        }
        return base + (first ? 2 : 0) + (second ? 8 : 0);
    }

    private static void emitQuadrant(
            float[][] face,
            float[] bounds,
            int cell,
            int baseMaterial,
            int sheetMaterial,
            FaceLighting.Sample light,
            TileModelView target
    ) {
        float[] bottomLeft = interpolate(face, bounds[0], bounds[1]);
        float[] bottomRight = interpolate(face, bounds[2], bounds[1]);
        float[] topRight = interpolate(face, bounds[2], bounds[3]);
        float[] topLeft = interpolate(face, bounds[0], bounds[3]);
        int start = target.add(2);
        TileModel mesh = target.getTileModel();
        positions(mesh, start, bottomLeft, bottomRight, topRight);
        positions(mesh, start + 1, bottomLeft, topRight, topLeft);

        boolean ctm = cell < 16;
        int grid = ctm ? 4 : 2;
        int index = ctm ? cell : cell - 16;
        int column = index % grid;
        int row = index / grid;
        float u0 = column / (float) grid;
        float u1 = (column + 1F) / grid;
        float v0 = row / (float) grid;
        float v1 = (row + 1F) / grid;
        mesh.setUvs(start, u0, v1, u1, v1, u1, v0);
        mesh.setUvs(start + 1, u0, v1, u1, v0, u0, v0);
        int material = ctm ? sheetMaterial : baseMaterial;
        for (int triangle = start; triangle < start + 2; triangle++) {
            mesh.setMaterialIndex(triangle, material);
            mesh.setColor(triangle, 1F, 1F, 1F);
            mesh.setAOs(triangle, 1F, 1F, 1F);
            mesh.setSunlight(triangle, light.sunlight());
            mesh.setBlocklight(triangle, light.blocklight());
        }
    }

    private static float[] interpolate(float[][] p, float u, float v) {
        float bottom = 1F - v;
        float left = 1F - u;
        return new float[]{
                left * bottom * p[0][0] + u * bottom * p[1][0]
                        + u * v * p[2][0] + left * v * p[3][0],
                left * bottom * p[0][1] + u * bottom * p[1][1]
                        + u * v * p[2][1] + left * v * p[3][1],
                left * bottom * p[0][2] + u * bottom * p[1][2]
                        + u * v * p[2][2] + left * v * p[3][2]
        };
    }

    private static boolean surfaceConnected(
            String blockId,
            BlockNeighborhood block,
            Direction face,
            Direction first
    ) {
        return surfaceConnected(blockId, block, face, first, null);
    }

    private static boolean surfaceConnected(
            String blockId,
            BlockNeighborhood block,
            Direction face,
            Direction first,
            Direction second
    ) {
        var firstVector = first.toVector();
        int x = firstVector.getX();
        int y = firstVector.getY();
        int z = firstVector.getZ();
        if (second != null) {
            var secondVector = second.toVector();
            x += secondVector.getX();
            y += secondVector.getY();
            z += secondVector.getZ();
        }
        String candidate = block.getNeighborBlock(x, y, z)
                .getBlockState().getId().getFormatted();
        if (!surfaceMatches(blockId, candidate)) {
            return false;
        }
        var normal = face.toVector();
        String outside = block.getNeighborBlock(
                x + normal.getX(), y + normal.getY(), z + normal.getZ()
        ).getBlockState().getId().getFormatted();
        return !surfaceMatches(blockId, outside);
    }

    private static boolean surfaceMatches(String blockId, String candidate) {
        if (STRUCTURAL.equals(blockId)) {
            return STRUCTURAL.equals(candidate);
        }
        return (REACTOR.equals(blockId) || LASER.equals(blockId))
                && (REACTOR.equals(candidate) || LASER.equals(candidate));
    }

    private static FaceAxes axes(Direction face) {
        return switch (face) {
            case SOUTH -> new FaceAxes(Direction.UP, Direction.EAST);
            case NORTH -> new FaceAxes(Direction.UP, Direction.WEST);
            case WEST -> new FaceAxes(Direction.UP, Direction.SOUTH);
            case EAST -> new FaceAxes(Direction.UP, Direction.NORTH);
            case UP -> new FaceAxes(Direction.NORTH, Direction.EAST);
            case DOWN -> new FaceAxes(Direction.SOUTH, Direction.EAST);
        };
    }

    private static void positions(TileModel mesh, int index, float[] a, float[] b, float[] c) {
        mesh.setPositions(index, a[0], a[1], a[2], b[0], b[1], b[2], c[0], c[1], c[2]);
    }

    private record FaceAxes(Direction top, Direction right) {
    }
}
