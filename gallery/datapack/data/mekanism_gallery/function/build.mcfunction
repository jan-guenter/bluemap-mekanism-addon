function mekanism_gallery:clear
fill 163 99 163 249 99 242 minecraft:smooth_stone

# Forty composite-model representatives: core factories and stable machine exteriors.
setblock 166 100 166 mekanism:basic_combining_factory
setblock 173 100 166 mekanism:basic_compressing_factory
setblock 180 100 166 mekanism:basic_crushing_factory
setblock 187 100 166 mekanism:basic_enriching_factory
setblock 194 100 166 mekanism:basic_infusing_factory
setblock 201 100 166 mekanism:basic_injecting_factory
setblock 208 100 166 mekanism:basic_purifying_factory
setblock 215 100 166 mekanism:basic_sawing_factory
setblock 222 100 166 mekanism:basic_smelting_factory
setblock 229 100 166 mekanism:antiprotonic_nucleosynthesizer
setblock 236 100 166 mekanism:chemical_crystallizer
setblock 243 100 166 mekanism:chemical_dissolution_chamber

setblock 166 100 173 mekanism:industrial_alarm
setblock 173 100 173 mekanism:isotopic_centrifuge
setblock 180 100 173 mekanism:nutritional_liquifier
setblock 187 100 173 mekanism:quantum_entangloporter
setblock 194 100 173 mekanism:solar_neutron_activator
setblock 201 100 173 mekanismgenerators:bio_generator
setblock 208 100 173 mekmm:basic_crystallizing_factory
setblock 215 100 173 mekmm:basic_dissolving_factory
setblock 222 100 173 mekmm:basic_lathing_factory
setblock 229 100 173 mekmm:basic_liquifying_factory
setblock 236 100 173 mekmm:basic_oxidizing_factory
setblock 243 100 173 mekmm:basic_painting_factory

setblock 166 100 180 mekmm:basic_pigment_extracting_factory
setblock 173 100 180 mekmm:basic_planting_factory
setblock 180 100 180 mekmm:basic_pressurised_reacting_factory
setblock 187 100 180 mekmm:basic_recycling_factory
setblock 194 100 180 mekmm:basic_replicating_factory
setblock 201 100 180 mekmm:basic_rolling_mill_factory
setblock 208 100 180 mekmm:basic_stamping_factory
setblock 215 100 180 mekmm:basic_washing_factory
setblock 222 100 180 mekmm:chemical_replicator
setblock 229 100 180 mekmm:fluid_replicator
setblock 236 100 180 mekmm:replicator
setblock 243 100 180 mekmm:large_antiprotonic_nucleosynthesizer

setblock 166 100 187 mekmm:large_solar_neutron_activator
setblock 173 100 187 mekanism:ultimate_smelting_factory
setblock 187 100 187 mekmm:basic_centrifuging_factory

# Every exact transmitter ID as a naturally connected pair.
setblock 165 100 202 mekanism:basic_universal_cable
setblock 166 100 202 mekanism:basic_universal_cable
setblock 178 100 202 mekanism:advanced_universal_cable
setblock 179 100 202 mekanism:advanced_universal_cable
setblock 191 100 202 mekanism:elite_universal_cable
setblock 192 100 202 mekanism:elite_universal_cable
setblock 204 100 202 mekanism:ultimate_universal_cable
setblock 205 100 202 mekanism:ultimate_universal_cable
setblock 217 100 202 mekanism:basic_mechanical_pipe
setblock 218 100 202 mekanism:basic_mechanical_pipe
setblock 230 100 202 mekanism:advanced_mechanical_pipe
setblock 231 100 202 mekanism:advanced_mechanical_pipe

setblock 165 100 209 mekanism:elite_mechanical_pipe
setblock 166 100 209 mekanism:elite_mechanical_pipe
setblock 178 100 209 mekanism:ultimate_mechanical_pipe
setblock 179 100 209 mekanism:ultimate_mechanical_pipe
setblock 191 100 209 mekanism:basic_pressurized_tube
setblock 192 100 209 mekanism:basic_pressurized_tube
setblock 204 100 209 mekanism:advanced_pressurized_tube
setblock 205 100 209 mekanism:advanced_pressurized_tube
setblock 217 100 209 mekanism:elite_pressurized_tube
setblock 218 100 209 mekanism:elite_pressurized_tube
setblock 230 100 209 mekanism:ultimate_pressurized_tube
setblock 231 100 209 mekanism:ultimate_pressurized_tube

setblock 165 100 216 mekanism:basic_thermodynamic_conductor
setblock 166 100 216 mekanism:basic_thermodynamic_conductor
setblock 178 100 216 mekanism:advanced_thermodynamic_conductor
setblock 179 100 216 mekanism:advanced_thermodynamic_conductor
setblock 191 100 216 mekanism:elite_thermodynamic_conductor
setblock 192 100 216 mekanism:elite_thermodynamic_conductor
setblock 204 100 216 mekanism:ultimate_thermodynamic_conductor
setblock 205 100 216 mekanism:ultimate_thermodynamic_conductor
setblock 217 100 216 mekanism:basic_logistical_transporter
setblock 218 100 216 mekanism:basic_logistical_transporter
setblock 230 100 216 mekanism:advanced_logistical_transporter
setblock 231 100 216 mekanism:advanced_logistical_transporter

setblock 165 100 223 mekanism:elite_logistical_transporter
setblock 166 100 223 mekanism:elite_logistical_transporter
setblock 178 100 223 mekanism:ultimate_logistical_transporter
setblock 179 100 223 mekanism:ultimate_logistical_transporter
setblock 191 100 223 mekanism:restrictive_transporter
setblock 192 100 223 mekanism:restrictive_transporter
setblock 204 100 223 mekanism:diversion_transporter
setblock 205 100 223 mekanism:diversion_transporter

# Explicit connection modes and stable T silhouettes.
setblock 165 100 234 mekanism:basic_universal_cable
setblock 166 100 234 mekanism:basic_universal_cable
setblock 167 100 234 mekanism:basic_universal_cable
setblock 166 100 233 mekanism:basic_universal_cable
setblock 178 100 234 mekanism:basic_mechanical_pipe
setblock 179 100 234 mekanism:basic_mechanical_pipe
setblock 180 100 234 mekanism:basic_mechanical_pipe
setblock 179 100 233 mekanism:basic_mechanical_pipe
setblock 191 100 234 mekanism:advanced_universal_cable
setblock 190 100 234 mekanism:basic_energy_cube[facing=east]
setblock 192 100 234 mekanism:basic_energy_cube[facing=west]
data merge block 191 100 234 {connections:0b,acceptors:48b,connection:[I;0,0,0,0,2,1]}

# Whole-block Covers: simple, directional, shaped and translucent stock models.
setblock 204 100 234 mekanism:basic_universal_cable
data merge block 204 100 234 {CoverState:"minecraft:bricks"}
setblock 208 100 234 mekanism:basic_universal_cable
data merge block 208 100 234 {CoverState:"minecraft:oak_log[axis=x]"}
setblock 212 100 234 mekanism:basic_universal_cable
data merge block 212 100 234 {CoverState:"minecraft:oak_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]"}
setblock 216 100 234 mekanism:basic_universal_cable
data merge block 216 100 234 {CoverState:"minecraft:glass"}

# Five energy-cube controls; the add-on treats their ports as static structure only.
setblock 226 100 232 mekanism:basic_energy_cube[facing=north]
data merge block 226 100 232 {component_config:{config0:[I;4,1,0,7,0,0]}}
setblock 230 100 232 mekanism:advanced_energy_cube[facing=east]
data merge block 230 100 232 {component_config:{config0:[I;4,1,0,7,0,0]}}
setblock 234 100 232 mekanism:elite_energy_cube[facing=south]
data merge block 234 100 232 {component_config:{config0:[I;4,1,0,7,0,0]}}
setblock 238 100 232 mekanism:ultimate_energy_cube[facing=west]
data merge block 238 100 232 {component_config:{config0:[I;4,1,0,7,0,0]}}
setblock 242 100 232 mekanism:creative_energy_cube[facing=up]
data merge block 242 100 232 {component_config:{config0:[I;4,1,0,7,0,0]}}

# Connected formed-glass panels and the deliberate structural↔laser non-connection.
fill 166 100 240 167 101 240 mekanism:structural_glass
fill 174 100 240 175 101 240 mekanismgenerators:reactor_glass
setblock 182 100 240 mekanismgenerators:reactor_glass
setblock 183 100 240 mekanismgenerators:laser_focus_matrix
setblock 182 101 240 mekanismgenerators:laser_focus_matrix
setblock 183 101 240 mekanismgenerators:laser_focus_matrix
setblock 190 100 240 mekanism:structural_glass
setblock 191 100 240 mekanismgenerators:laser_focus_matrix
setblock 190 101 240 mekanism:structural_glass
setblock 191 101 240 mekanismgenerators:laser_focus_matrix

# Compact stock multiblock-shell controls; animated internals and fill are excluded.
fill 202 100 239 204 102 241 mekanism:boiler_casing hollow
setblock 203 101 239 mekanism:boiler_valve

scoreboard players set #ready mekanism_gallery 1
tellraw @a [{"text":"Mekanism BlueMap gallery built inside x160..255, z160..255.","color":"aqua"}]
