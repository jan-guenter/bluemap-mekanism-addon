/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.mekanism.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;

/** Retains only the six persistent energy-side modes. */
public final class EnergyCubeBlockEntityData extends MCABlockEntity {

    private ComponentConfig component_config;

    public EnergyCubeBlockEntityData() {
    }

    boolean hasPort(int relativeSide) {
        if (component_config == null || component_config.config0 == null
                || relativeSide < 0 || relativeSide >= component_config.config0.length) {
            return false;
        }
        int mode = component_config.config0[relativeSide];
        return mode == 1 || mode == 4 || mode == 7;
    }

    /** BlueNBT target for the nested {@code component_config} compound. */
    public static final class ComponentConfig {
        private int[] config0;

        public ComponentConfig() {
        }
    }
}
