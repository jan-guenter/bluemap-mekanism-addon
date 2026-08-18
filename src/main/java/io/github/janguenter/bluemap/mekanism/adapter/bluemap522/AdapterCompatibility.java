/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.mekanism.adapter.bluemap522;

import de.bluecolored.bluemap.core.BlueMap;

/** Exact BlueMap 5.22 runtime identities whose internal ABI is used here. */
public final class AdapterCompatibility {

    private AdapterCompatibility() {
    }

    public static boolean currentRuntimeSupported() {
        return ("5.22".equals(BlueMap.VERSION)
                && "fe5115d5548a30d34175b8e0449aaca280af199f".equals(BlueMap.GIT_HASH))
                || ("5.22-agent.backport-5.22-mc1.21.1-2".equals(BlueMap.VERSION)
                && "9be321df995a1103808621d529eb72773e719d4d".equals(BlueMap.GIT_HASH));
    }
}
