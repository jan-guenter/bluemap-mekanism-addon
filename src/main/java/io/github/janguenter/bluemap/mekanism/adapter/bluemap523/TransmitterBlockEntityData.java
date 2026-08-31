/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.mekanism.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

/** Retains only persisted topology, connection modes and whole-block cover state. */
public final class TransmitterBlockEntityData extends MCABlockEntity {

    private Object connections;
    private Object acceptors;
    private Object color;
    private int[] connection;

    @NBTName("CoverState")
    private String coverState;

    public TransmitterBlockEntityData() {
    }

    int transmitterConnections() {
        return unsignedByte(connections);
    }

    int acceptorConnections() {
        return unsignedByte(acceptors);
    }

    int connectionMode(int side) {
        if (connection == null || side < 0 || side >= connection.length) {
            return 0;
        }
        int mode = connection[side];
        return mode >= 0 && mode <= 3 ? mode : 0;
    }

    String coverState() {
        return coverState;
    }

    boolean hasColor() {
        return color instanceof Number;
    }

    private static int unsignedByte(Object value) {
        return value instanceof Byte byteValue ? Byte.toUnsignedInt(byteValue)
                : value instanceof Number number ? number.intValue() & 0xFF : 0;
    }
}
