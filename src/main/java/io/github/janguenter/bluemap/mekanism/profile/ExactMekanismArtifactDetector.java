/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.mekanism.profile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Finds the four exact Mekanism-family artifacts installed by All the Mons 1.2.0. */
public final class ExactMekanismArtifactDetector {

    /** Exact artifact role. */
    public enum Role {
        CORE,
        GENERATORS,
        COVERS,
        MORE_MACHINE
    }

    private static final Map<Role, Identity> IDENTITIES = Map.of(
            Role.CORE, new Identity(11_976_009L,
                    "004dbc9f3106f4d192aeaa1ee1190dd16ec9ca8059ed3d093b80034f4c574f43"),
            Role.GENERATORS, new Identity(1_114_598L,
                    "0e5783b111e756f27b48c62b2f0e02fff750c77f7985ff809bfadc5f444ba4ac"),
            Role.COVERS, new Identity(136_513L,
                    "7e67b8f5c111ef0d94abcd0c24580fa74c0532dbfa9e5cdbf58521e57cbbdc95"),
            Role.MORE_MACHINE, new Identity(2_983_024L,
                    "aebd1136a2e328a23d1801c768b387cf336bc1ebdab40770274d068bdaac9a12")
    );

    private ExactMekanismArtifactDetector() {
    }

    /** Returns a complete bundle only when all four exact artifacts are present. */
    public static Optional<Bundle> find(Iterable<Path> roots) {
        List<Path> candidates = new ArrayList<>();
        int inspected = 0;
        for (Path root : roots) {
            if (++inspected > 8_192 || Thread.currentThread().isInterrupted()) {
                return Optional.empty();
            }
            if (root != null && Files.isRegularFile(root)) {
                candidates.add(root);
            }
        }

        EnumMap<Role, Path> found = new EnumMap<>(Role.class);
        for (Path candidate : candidates) {
            try {
                long size = Files.size(candidate);
                Role possible = null;
                for (Map.Entry<Role, Identity> entry : IDENTITIES.entrySet()) {
                    if (entry.getValue().size() == size) {
                        possible = entry.getKey();
                        break;
                    }
                }
                if (possible != null && IDENTITIES.get(possible).sha256().equals(digest(candidate))) {
                    found.put(possible, candidate.toRealPath());
                }
            } catch (IOException exception) {
                return Optional.empty();
            }
        }
        return found.size() == Role.values().length
                ? Optional.of(new Bundle(Map.copyOf(found))) : Optional.empty();
    }

    private static String digest(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[64 * 1_024];
            try (InputStream input = Files.newInputStream(path)) {
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    digest.update(buffer, 0, read);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    /** Exact installed artifact set. */
    public record Bundle(Map<Role, Path> paths) {
        public Path path(Role role) {
            return paths.get(role);
        }
    }

    private record Identity(long size, String sha256) {
    }
}
