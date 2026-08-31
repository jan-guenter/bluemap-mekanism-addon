# BlueMap Mekanism Add-on

[![CI](https://github.com/jan-guenter/bluemap-mekanism-addon/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/jan-guenter/bluemap-mekanism-addon/actions/workflows/ci.yml)

An exact-profile BlueMap 5.23 feature-backport add-on for the stable Mekanism
family appearance missing from BlueMap in All the Mons 1.2.0.

## Status and compatibility

Version `0.1.0-alpha.2` is an unpublished migration candidate for this exact
environment. It preserves the owner-accepted `0.1.0-alpha.1` rendering scope.
Compatibility outside these inputs is not asserted.

## Visual scope

The add-on targets only:

- All the Mons `1.2.0`, Minecraft `1.21.1`, NeoForge `21.1.248`, Java 21;
- BlueMap feature backport
  `5.22-feature.backport-5.23-stateless-java-web-server-46` at commit
  `7e07f4e74ec1e92a6ead9aa1e66054af3e133aac` and API commit
  `285c9a60eff3ac2b0cab308ce1058d1565be0971`;
- Mekanism and Mekanism Generators `1.21.1-10.7.19.85`, Mekanism Covers
  `1.3-BETA+1.21`, and Mekanism: MoreMachine `1.21.1-1.3.3`, with the exact
  byte identities in `provenance/upstreams.json`.

It flattens NeoForge composite machine models, renders persistent Mekanism
transmitter topology and whole-block Mekanism Covers camouflage, applies
persisted Energy Cube port configuration, and connects Structural Glass,
Reactor Glass, and Laser Focus Matrix surfaces.

Transported contents, fill state, screens, LEDs, and animations remain outside
the static map view. Missing inputs, different artifacts, or unsupported data
leave stock BlueMap rendering unchanged. The add-on writes nothing to the
world.

## Build and verification

Clone with `--recurse-submodules`, or initialize the two pinned support
modules in an existing checkout:

```bash
git submodule update --init --recursive -- \
  tooling/bluemap-addon-toolkit modules/bluemap-addon-adapter-api
gradle --no-daemon \
  -PbluemapSourcePath=/path/to/exact/bluemap \
  -PmekanismJar=/path/to/Mekanism-1.21.1-10.7.19.85.jar \
  -PmekanismGeneratorsJar=/path/to/MekanismGenerators-1.21.1-10.7.19.85.jar \
  -PmekanismCoversJar=/path/to/mekanismcovers-1.3-BETA+1.21.jar \
  -PmoreMachineJar=/path/to/mekmm-1.21.1-1.3.3.jar \
  clean prototypeCheck build generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication
```

The settings preflight accepts only the committed support gitlinks and exact
BlueMap source identities. `prototypeCheck` verifies all four installed mod
artifacts, the adapter boundary, archive boundaries, tests, and gallery.

Tagged releases publish production/source JARs, POM, Gradle module metadata,
and checksums on GitHub Releases and Maven coordinates
`io.github.jan-guenter:bluemap-mekanism-addon:<version>` on GitHub Packages.

## Installation

Place `bluemap-mekanism-addon-0.1.0-alpha.2.jar` in `config/bluemap/packs`,
make the four exact mod JARs available to BlueMap's resource scan, restart,
and rerender the affected area. Do not place this add-on in `mods`.

The gallery datapack in the source tree is only a disposable visual-review
fixture and is excluded from release assets.

## License and provenance

This project is released under the [MIT License](LICENSE). It bundles no
Mekanism-family resources or binaries. See [NOTICE.md](NOTICE.md),
[THIRD_PARTY.md](THIRD_PARTY.md), and
[provenance/upstreams.json](provenance/upstreams.json).
