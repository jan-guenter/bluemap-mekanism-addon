# Changelog

## 0.1.0-alpha.2 - 2026-08-31

- Target only BlueMap feature-backport commit
  `7e07f4e74ec1e92a6ead9aa1e66054af3e133aac` and API commit
  `285c9a60eff3ac2b0cab308ce1058d1565be0971`.
- Move the local adapter boundary from `bluemap522` to `bluemap523`.
- Compile the four shared Adapter API `0.1.0-alpha.2` sources and remove the
  duplicate runtime, registry, and resource-extension helpers.
- Preserve all accepted composite model, transmitter, cover, Energy Cube,
  connected-glass, stock-fallback, and gallery behavior.

## 0.1.0-alpha.1 - 2026-08-25

- Flatten exact NeoForge composite machine models for BlueMap.
- Render persistent Mekanism transmitter topology from installed OBJ models.
- Render exact Mekanism Covers whole-block camouflage state.
- Render persisted Energy Cube port configuration.
- Render connected Structural Glass, Reactor Glass and Laser Focus Matrix
  surfaces with their exact CTM neighborhood rules.
