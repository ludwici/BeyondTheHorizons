# Ad Astra: Beyond The Horizons

Ad Astra: Beyond The Horizons is a work-in-progress add-on for [Ad Astra](https://github.com/terrarium-earth/Ad-Astra). The first explorable planet is Neptune, with a floating ice shell and a deep subsurface ocean.

## Development roadmap

See [ROADMAP.md](ROADMAP.md) for current status, gameplay milestones, methane industry, atmospheric platforms, probes, Neptune pressure hazards, gas giants and interstellar navigation. Detailed design notes live in [`docs/planning/`](docs/planning/).

## Supported versions

| Component | Version |
| --- | --- |
| Minecraft | 1.21.1 |
| Java | 21 |
| Ad Astra | 1.16.26 |
| NeoForge | 21.1.249 |
| Fabric Loader | 0.19.2 |
| Fabric API | 0.116.15 |
| Gradle | 9.4.0 |
| Cloche | 0.18.15 |

The loader and tooling versions follow the Ad Astra 1.21.1 branch.

## Project structure

- `src/main/java` — shared Java sources for both loaders.
- `src/fabric/java` — Fabric-specific entrypoint and future integrations.
- `src/neoforge/java` — NeoForge-specific entrypoint and future integrations.
- `build.gradle.kts` — multi-loader build using Cloche.
- `.github/workflows/build.yml` — NeoForge-only CI and downloadable JAR artifact.

## Build locally

Install Java 21. The project provides Gradle wrapper scripts; the wrapper binary is included in the repository.

On Windows:

```powershell
.\gradlew.bat build
```

On Linux/macOS:

```bash
./gradlew build
```

To build NeoForge only locally, run `./gradlew neoforgeIncludeJar` (Windows: `.\gradlew.bat neoforgeIncludeJar`).

The distributable NeoForge JAR is `build/libs/BeyondTheHorizons-neoforge-1.21.1-0.1.0.jar`. The mod display name is `Ad Astra: Beyond The Horizons`, while the JAR filename uses only `BeyondTheHorizons`.

## Neptune environmental pressure (NeoForge)

Pressure Abyss now has a server-side pressure hazard. Underwater pressure increases with depth; unprotected players receive a warning and begin taking armor-bypassing pressure damage after about three seconds. This feature is implemented only on NeoForge and only within Neptune's deep `pressure_abyss` biome, where unpressurized air pockets are not automatically safe. It does not change Ad Astra oxygen mechanics.

The new `beyond_the_horizons:pressure_resistance` attribute (default **4.5 bar**) is intended for upcoming diving suits and sealed vehicles. These items are **not yet implemented**; operators can temporarily test protection using `/attribute @s beyond_the_horizons:pressure_resistance base set 30`, then reset to `4.5`.

- `/bth environment debug` — toggle on-screen environmental diagnostics (operator command).
- `/bth environment inspect` — print current pressure, protection and other fields (operator command).

See [test instructions](docs/planning/environment-pressure-implementation.md). Wind and radiation are currently informational placeholders, not active damage systems.

### Expedition diving suit and abyss visibility

The first **Expedition Diving Suit** can be crafted from steel/Desh components: helmet, chestplate, leggings and boots. All BeyondTheHorizons items are shown in a dedicated **Ad Astra: Beyond The Horizons** creative tab. Wearing the entire set adds **3 bar** of pressure protection (default total **7.5 bar**). This protects only shallower depths, not the deepest Pressure Abyss. The chestpiece reuses Ad Astra oxygen storage and underwater breathing. The current suit appearance reuses Ad Astra art while dedicated textures are still being designed.

Pressure Abyss has a stronger blue-black underwater fog and shorter visibility on NeoForge, with smooth time-based entry/exit and a 16-block depth ramp. This is rendered client-side and may vary with third-party shaders.

See [diving-suit test plan](docs/planning/expedition-diving-suit.md).

### SpaceY Workbench — modular probe assembly (NeoForge)

The standalone **SpaceY Workbench** is crafted with steel plates, Ostrum plates, Desh cables, a comparator and an ordinary crafting table. NASA Workbench remains dedicated to rockets; SpaceY recipes do not reference it.

SpaceY Workbench has six component positions: hull, propulsion (water or atmosphere), battery, antenna, optional pressure shell and sample collector. It assembles a **Modular Probe** item with saved module configuration in item custom data (including environment and preliminary pressure rating). The workbench inventory persists in NBT and drops its stored components when broken. To replace modules, open a SpaceY Workbench with all six input slots empty, pick up an assembled probe from your inventory onto the cursor, then press **Disassemble**. The probe is consumed and its original components appear in the input slots; replace desired parts and assemble again.

The component and block icons currently reuse existing Minecraft and Ad Astra textures; original textures are still pending. **Physical probe launch, navigation and resource collection are not implemented yet.** See [probe design and implementation status](docs/planning/probe-modules-and-progression.md).

### Lunar crystal optics — first return-to-Moon progression

The laser drill extracts lunar crystals from rare Moon geodes. The first industrial processing chain reuses **Ad Astra's Compressor** rather than introducing another machine:

1. Compress one `lunar_crystal` into one `polished_lunar_crystal` (`ad_astra:compressing`, 120 ticks, recipe energy 20).
2. Craft a `precision_optical_lens` from the polished crystal, three glass panes, and one Desh plate.
3. Install the lens in the `probe_antenna` crafting recipe; the optical component replaces the former redstone torch.

Both items are registered for NeoForge 1.21.1 with their own textures, models and translations. The **SpaceY Workbench recipe is intentionally unchanged** while later planet materials remain under design. These processing and assembly steps require in-game verification.

## GitHub Actions

Open **Actions → Build NeoForge JAR → Run workflow** to build on demand. Builds run on pushes to `main` and pull requests. GitHub Actions builds **NeoForge only**; Fabric sources are kept for future work. Download the NeoForge JAR from **Artifacts** (retention **1 day**). Nothing is automatically published to CurseForge or Modrinth.

## Dependencies

Ad Astra is a required runtime dependency on both platforms. Fabric also requires Fabric API. Ad Astra's own additional required dependencies must be installed according to its release metadata.

## License

No open-source license has been granted for this add-on. Ad Astra is a separate mod owned by its respective authors.

## Neptune (Solar System)

Neptune is a Tier 4 Ad Astra destination at `beyond_the_horizons:neptune` with an orbit at `beyond_the_horizons:neptune_orbit`.

- Gravity: **11.15 m/s²**; temperature: **−210 °C**; oxygen: **none**; solar power: **1**.
- Surface: **packed ice**, with rolling, noise-shaped hills and shallow troughs from **Y 171 to Y 197** (nominal Y 184), generated in broad connected shapes.
- Transition: **packed and blue ice** follows an independent, three-scale coherent noise boundary, with a maximum deviation of **13 blocks**. At least 12 packed-ice blocks remain under the surface.
- Lower ice cap: thick **blue ice** above the deep ocean; its underside gently varies by up to **16 blocks** in broad, continuous shapes, without disconnected ice blobs.
- Ocean: **source water** from the ice boundary (normally Y 85, varying between Y 69 and 101) to the **5-block bedrock floor** at Y −64 to −60.
- World generation uses a **custom layered generator that delegates block filling to vanilla FlatLevelSource**, with a 3D biome source for four real biomes. The generator preserves a reliable full source-water ocean. A single feature runs **only when a chunk generates**, adjusting the water/blue-ice and blue-ice/packed-ice boundaries with deterministic world-coordinate noise. The resulting ice shell is solid, has no isolated blocks, and joins seamlessly across chunks. The packed-ice surface also receives three-scale world-coordinate noise for seamless low rolling ridges and valleys.
- Thick distance fog is disabled so players can see the surrounding landscape while landing.
- Ice-boundary shaping happens only at chunk generation; **already-generated chunks will not be reconstructed**. Test in a fresh world or new unvisited region.
- **Ad Astra's random environmental freezing of water is disabled only for Neptune's water.** Ad Astra normally freezes water source blocks throughout a cold planet at random positions regardless of depth, so an enclosed deep ocean would otherwise fill with isolated frozen blocks in loaded chunks. Neptune's temperature remains −210 °C, and other Ad Astra environmental reactions still run. Already frozen blocks are not removed automatically.

Neptune is interpreted as a fictional walkable ice-shell world; real Neptune has no known solid surface.

### Neptune biomes

All four biomes are assigned by a smooth world-coordinate biome source, with actual per-biome water and fog colors (F3 reports the selected biome at the player's height).

- **Cobalt Plains**: common packed/blue-ice surface, gently rolling ridges.
- **Methane Drifts**: snow-covered ice drifts, with scattered powder snow; currently uses vanilla blocks, not a methane resource.
- **Neptunian Ocean**: the default deep underwater biome.
- **Pressure Abyss**: rarer deep-water regions below Y 40, with almost-black water/fog and irregular basalt/deepslate deposits on the ocean floor.

Surface variations are built from existing blocks: thin snow-block drifts and rare powder-snow pockets in Methane Drifts, plus blue-ice seams in Cobalt Plains. Pressure Abyss uses a solid basalt/deepslate/tuff ocean-floor layer, with occasional amethyst exposures (all vanilla); no exposed air gaps are created in the deep ocean. Biome patches follow the same deterministic map across chunk borders and are independent of the world's random seed in this initial implementation. Existing chunks retain their old biome data and blocks; test in a new world. The ice-shell and water-freezing fixes remain in place.
