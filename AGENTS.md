# Agent guide for BlueMap Mekanism Add-on

This is an independent public BlueMap add-on repository in the BlueMap ATMons
portfolio. Read this file and `README.md` before changing it.

## Exact baseline

- All the Mons `1.2.0`
- Minecraft `1.21.1`
- NeoForge `21.1.248`
- Java `21`
- BlueMap feature backport
  `5.22-feature.backport-5.23-stateless-java-web-server-46`, commit
  `7e07f4e74ec1e92a6ead9aa1e66054af3e133aac`
- BlueMap API commit `285c9a60eff3ac2b0cab308ce1058d1565be0971`
- Adapter API `0.1.0-alpha.2`, commit
  `e81f08bc4bfbf02d810ec8949a019130e2e61634`, source tree
  `2f974c9bb2ba13888d69682f86f30f58922d30eb`
- Add-on ID `bluemap-mekanism`, version `0.1.0-alpha.2`

This is a standalone BlueMap add-on, not a NeoForge mod. Do not add client
classes, candidate binaries or assets, nested JARs, Minecraft classes, Mixins,
or world state.

## Development contract

- Preserve stock rendering when the exact runtime profile is absent,
  unsupported, malformed, duplicated, or disabled.
- Keep BlueMap internals isolated below the versioned adapter package.
- Compile the exact Adapter API sources into the add-on. Do not nest its JAR.
- Keep exact candidate identities and resource contracts in the profile.
- Preserve the accepted composite models, transmitter topology, covers,
  Energy Cube ports, connected glass, and gallery behavior.
- Keep live contents, flow, fill levels, LEDs, and animation outside scope.
- Follow the shared `addon-v1` source style maintained by `bluemap-atmons`.

## Commands

Run the repository's documented exact-input properties, then:

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

Follow `docs/RELEASING.md` and the repository workflow for publication. Never
stage generated build output, candidate JARs, worlds, credentials, logs, or
local research evidence.
