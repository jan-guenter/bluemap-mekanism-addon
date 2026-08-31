# Releasing

Release only an owner-accepted renderer from a clean reviewed commit.

Before running release gates, initialize the pinned toolkit submodule and
install the exact development-only toolkit into a Python 3.11 or newer
environment:

```bash
git submodule update --init --recursive -- \
  tooling/bluemap-addon-toolkit modules/bluemap-addon-adapter-api
python -m pip install --disable-pip-version-check --no-deps \
  --require-hashes --only-binary=:all: \
  --requirement requirements/toolkit.txt
```

The requirement locks the 20,585-byte `v0.3.0-alpha.1` wheel at SHA-256
`82f1ec53603646849a7c2d4b58f3fb7000413fe83043a302bee88cc88daeb8f7`.

1. Confirm the exact All the Mons, Minecraft, NeoForge, Java, BlueMap, and
   four mod-artifact identities documented by this repository.
2. Run `prototypeCheck` and the publication gates with
   `-PbluemapSourcePath`, `-PmekanismJar`, `-PmekanismGeneratorsJar`,
   `-PmekanismCoversJar`, and `-PmoreMachineJar`.
3. Verify the production and sources JAR boundaries, licenses, notices, and
   provenance. Do not bundle mod binaries, resources, source, galleries,
   worlds, logs, or credentials.
4. Confirm `addon_version`, the intended Maven coordinates, and every sealed
   release size and SHA-256 value.
5. Merge through a pull request. Create an immutable annotated tag exactly
   equal to `v<addon_version>` at the reviewed commit.
6. Let `.github/workflows/release.yml` publish. Compare downloaded assets and
   checksums with the accepted local artifacts before updating
   `bluemap-atmons`.

A tooling-only conventions change does not alter `addon_version` or an
existing tag. No publication step deploys to a production server.
