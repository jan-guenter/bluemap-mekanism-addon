# BlueMap Mekanism Add-on

Experimental BlueMap 5.22 add-on for the exact Mekanism family shipped by All
the Mons 1.2.0. It fills the two large static-map gaps that affect building
appearance: NeoForge composite machine models and connected Mekanism
transmitters, including whole-block Mekanism Covers camouflage.

It also renders persisted Energy Cube port configuration and connected
Structural Glass, Reactor Glass and Laser Focus Matrix CTM surfaces.

The add-on reads models and textures from the installed mod JARs. It does not
bundle or redistribute Mekanism resources. Transported contents, fill state,
screens, LEDs and animations are deliberately omitted.

Supported exact artifacts:

- Mekanism `1.21.1-10.7.19.85`
- Mekanism Generators `1.21.1-10.7.19.85`
- Mekanism Covers `1.3-BETA+1.21`
- Mekanism: MoreMachine `1.21.1-1.3.3`

Clone with submodules so the exact reviewed build convention is available:

```bash
git clone --recurse-submodules \
  https://github.com/jan-guenter/bluemap-mekanism-addon.git
```

For an existing checkout, run `git submodule update --init --recursive`. The
build rejects an uninitialized, dirty, or incorrectly pinned toolkit
submodule. Build with `gradle --no-daemon clean build` on Java 21. The
production JAR is written to `build/libs/`; the compact review datapack is
written to `build/gallery/bluemap-mekanism-gallery.zip`.

## Installation

Place the release JAR in BlueMap's `packs/` directory, alongside the four
supported mod JARs or their existing `.zip` aliases, then restart BlueMap.
Clients do not need the add-on. The gallery datapack is review-only and is not
part of the release.
