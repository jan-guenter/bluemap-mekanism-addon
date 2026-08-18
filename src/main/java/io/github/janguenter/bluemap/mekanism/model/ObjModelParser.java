/*
 * SPDX-License-Identifier: MIT
 *
 * Small independently authored parser for the public Wavefront OBJ/MTL text
 * format used by the operator-installed Mekanism resources.
 */
package io.github.janguenter.bluemap.mekanism.model;

import io.github.janguenter.bluemap.mekanism.model.ObjModel.Triangle;
import io.github.janguenter.bluemap.mekanism.model.ObjModel.Vertex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Bounded parser for positions, UVs, object groups, materials and polygon faces. */
public final class ObjModelParser {

    private static final int MAX_LINES = 100_000;

    private ObjModelParser() {
    }

    public static ObjModel parse(byte[] raw) throws IOException {
        List<Vec3> positions = new ArrayList<>();
        List<Vec2> uvs = new ArrayList<>();
        Map<String, List<Triangle>> groups = new LinkedHashMap<>();
        String group = null;
        String material = null;
        int lineCount = 0;

        try (BufferedReader reader = new BufferedReader(new StringReader(
                new String(raw, StandardCharsets.UTF_8)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (++lineCount > MAX_LINES) {
                    throw new IOException("OBJ exceeds line bound");
                }
                line = line.trim();
                if (line.isEmpty() || line.charAt(0) == '#') {
                    continue;
                }
                String[] tokens = line.split("\\s+");
                switch (tokens[0]) {
                    case "v" -> {
                        require(tokens, 4, "position");
                        positions.add(new Vec3(number(tokens[1]), number(tokens[2]),
                                number(tokens[3])));
                    }
                    case "vt" -> {
                        require(tokens, 3, "uv");
                        uvs.add(new Vec2(number(tokens[1]), 1F - number(tokens[2])));
                    }
                    case "o", "g" -> {
                        require(tokens, 2, "group");
                        group = tokens[1];
                        groups.computeIfAbsent(group, ignored -> new ArrayList<>());
                    }
                    case "usemtl" -> {
                        require(tokens, 2, "material");
                        material = tokens[1];
                    }
                    case "f" -> {
                        if (group == null || material == null || tokens.length < 4) {
                            throw new IOException("face missing group, material or vertices");
                        }
                        List<Vertex> polygon = new ArrayList<>(tokens.length - 1);
                        for (int index = 1; index < tokens.length; index++) {
                            polygon.add(vertex(tokens[index], positions, uvs));
                        }
                        for (int index = 1; index + 1 < polygon.size(); index++) {
                            groups.get(group).add(new Triangle(
                                    polygon.get(0), polygon.get(index), polygon.get(index + 1),
                                    material
                            ));
                        }
                    }
                    default -> {
                        // Normals, libraries, smoothing groups and comments are not needed.
                    }
                }
            }
        }
        if (groups.size() != 24 || groups.values().stream().anyMatch(List::isEmpty)) {
            throw new IOException("unexpected Mekanism transmitter OBJ group roster");
        }
        Map<String, List<Triangle>> frozen = new LinkedHashMap<>();
        groups.forEach((name, triangles) -> frozen.put(name, List.copyOf(triangles)));
        return new ObjModel(frozen);
    }

    /** Parses MTL material names to the {@code #texture_variable} used by the model. */
    public static Map<String, String> parseMaterials(byte[] raw) throws IOException {
        Map<String, String> result = new LinkedHashMap<>();
        String current = null;
        int lineCount = 0;
        try (BufferedReader reader = new BufferedReader(new StringReader(
                new String(raw, StandardCharsets.UTF_8)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (++lineCount > 1_000) {
                    throw new IOException("MTL exceeds line bound");
                }
                line = line.trim();
                if (line.startsWith("newmtl ")) {
                    current = line.substring(7).trim();
                } else if (line.startsWith("map_Kd ") && current != null) {
                    String variable = line.substring(7).trim();
                    if (!variable.startsWith("#") || variable.length() < 2) {
                        throw new IOException("expected MTL texture variable");
                    }
                    result.put(current, variable.substring(1));
                }
            }
        }
        if (result.isEmpty()) {
            throw new IOException("MTL has no texture mappings");
        }
        return Map.copyOf(result);
    }

    private static Vertex vertex(String token, List<Vec3> positions, List<Vec2> uvs)
            throws IOException {
        String[] indices = token.split("/", -1);
        if (indices.length < 2 || indices[0].isEmpty() || indices[1].isEmpty()) {
            throw new IOException("OBJ face lacks position or UV index");
        }
        int positionIndex = resolveIndex(indices[0], positions.size());
        int uvIndex = resolveIndex(indices[1], uvs.size());
        Vec3 position = positions.get(positionIndex);
        Vec2 uv = uvs.get(uvIndex);
        return new Vertex(position.x(), position.y(), position.z(), uv.u(), uv.v());
    }

    private static int resolveIndex(String token, int size) throws IOException {
        try {
            int raw = Integer.parseInt(token);
            int index = raw > 0 ? raw - 1 : size + raw;
            if (index < 0 || index >= size) {
                throw new IOException("OBJ index out of range");
            }
            return index;
        } catch (NumberFormatException exception) {
            throw new IOException("invalid OBJ index", exception);
        }
    }

    private static float number(String token) throws IOException {
        try {
            float value = Float.parseFloat(token);
            if (!Float.isFinite(value)) {
                throw new IOException("non-finite OBJ number");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IOException("invalid OBJ number", exception);
        }
    }

    private static void require(String[] tokens, int length, String kind) throws IOException {
        if (tokens.length < length) {
            throw new IOException("short OBJ " + kind);
        }
    }

    private record Vec3(float x, float y, float z) {
    }

    private record Vec2(float u, float v) {
    }
}
