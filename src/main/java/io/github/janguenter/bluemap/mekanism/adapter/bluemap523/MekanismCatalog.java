/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.mekanism.adapter.bluemap523;

import java.util.LinkedHashSet;
import java.util.Set;

/** Exact Mekanism 10.7.19 transmitter roster and family classification. */
final class MekanismCatalog {

    static final Set<String> TRANSMITTERS = transmitters();
    static final Set<String> ENERGY_CUBES = Set.of(
            "mekanism:basic_energy_cube",
            "mekanism:advanced_energy_cube",
            "mekanism:elite_energy_cube",
            "mekanism:ultimate_energy_cube",
            "mekanism:creative_energy_cube"
    );
    static final Set<String> CONNECTED_GLASS = Set.of(
            "mekanism:structural_glass",
            "mekanismgenerators:reactor_glass",
            "mekanismgenerators:laser_focus_matrix"
    );
    static final Set<String> CUSTOM_BLOCKS = customBlocks();
    static final Set<String> LARGE = Set.of(
            "mekanism:basic_mechanical_pipe",
            "mekanism:advanced_mechanical_pipe",
            "mekanism:elite_mechanical_pipe",
            "mekanism:ultimate_mechanical_pipe",
            "mekanism:basic_logistical_transporter",
            "mekanism:advanced_logistical_transporter",
            "mekanism:elite_logistical_transporter",
            "mekanism:ultimate_logistical_transporter",
            "mekanism:restrictive_transporter",
            "mekanism:diversion_transporter"
    );
    static final Set<String> GLASS = Set.of(
            "mekanism:basic_logistical_transporter",
            "mekanism:advanced_logistical_transporter",
            "mekanism:elite_logistical_transporter",
            "mekanism:ultimate_logistical_transporter",
            "mekanism:restrictive_transporter"
    );

    private MekanismCatalog() {
    }

    private static Set<String> transmitters() {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String tier : new String[]{"basic", "advanced", "elite", "ultimate"}) {
            result.add("mekanism:" + tier + "_universal_cable");
            result.add("mekanism:" + tier + "_mechanical_pipe");
            result.add("mekanism:" + tier + "_pressurized_tube");
            result.add("mekanism:" + tier + "_thermodynamic_conductor");
            result.add("mekanism:" + tier + "_logistical_transporter");
        }
        result.add("mekanism:restrictive_transporter");
        result.add("mekanism:diversion_transporter");
        return Set.copyOf(result);
    }

    private static Set<String> customBlocks() {
        LinkedHashSet<String> result = new LinkedHashSet<>(TRANSMITTERS);
        result.addAll(ENERGY_CUBES);
        result.addAll(CONNECTED_GLASS);
        return Set.copyOf(result);
    }
}
