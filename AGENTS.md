# BeyondTheHorizons — agent instructions

These instructions apply to work in this repository. The primary Minecraft target is **NeoForge 1.21.1**; the mod namespace is `beyond_the_horizons`.

## Visual style and references

1. **Ad Astra is the primary visual reference.** Inspect the actual relevant Ad Astra 1.21.1 game assets before creating a new texture, especially their silhouette, palette, shadows, highlight placement, and scale.
   - Reference: [Ad Astra assets](https://github.com/terrarium-earth/Ad-Astra/tree/1.21.1/src/main/resources/assets/ad_astra).
2. **Create is a secondary reference** for coherent material palettes, readable machinery, compact item shading, and effective limited-color pixel art. If the references conflict, prefer Ad Astra.
   - Reference: [Create assets](https://github.com/Creators-of-Create/Create/tree/mc1.21.1/dev/src/main/resources/assets/create).
3. Use references for visual analysis, not for copying protected artwork into BeyondTheHorizons. New assets should be original unless there is an explicit license-compatible reason to reuse an existing asset.
4. Preserve the existing BeyondTheHorizons color identity: sci-fi technology, lunar cyan/teal crystals, and coherent materials across raw, processed, and finished components.

## Non-negotiable texture workflow

When asked to create, redesign, or edit an in-game texture:

1. **Inspect the exact existing PNG and its model/blockstate** (and neighboring mod textures) before editing; identify the asset's actual dimensions, layout, alpha and intended game usage. If no existing source exists, inspect analogous Ad Astra textures.
2. **Deliver a real Minecraft resource, not a concept image.** The final output is a valid PNG at the asset's native resolution in `src/main/resources/assets/beyond_the_horizons/textures/...`. The user should not have to resize, redraw, rename, or manually copy generated images into the project.
3. Default to **16×16 RGBA** for ordinary inventory items and individual cube-face block textures, unless the existing model, vanilla convention, or a user requirement clearly calls for another size. GUI, entity skins, complex model UVs, and animated sprite strips must use their proper layouts — do not force everything to 16×16.
4. Draw/edit at the **target pixel grid** (e.g. Pillow, a pixel editor, or precise grid operations), one pixel at a time when needed. Use hard edges and intentional facets. No antialiasing, blur, interpolation, JPEG, or texture filtering. Use nearest-neighbor only to enlarge inspection previews.
5. A large AI-generated image (including 1024×1024 or 1254×1254) may serve as **concept reference only**. Do not submit it as the finished game texture or simply shrink it blindly to 16×16. Reconstruct, hand-correct, or redraw it at the native pixel grid.
6. Keep silhouette readable at 16×16 and at ordinary inventory scale. Favor a restrained palette with purposeful light/dark planes; highlights should identify material and form, not be random noise. Preserve transparent pixels outside the object.
7. If **editing an existing texture**, change only the requested visual elements. Keep item identity, model geometry/orientation, and unrelated assets unless the user requests a redesign.
8. Verify the resource file is readable, actual dimensions match requirements, alpha transparency works, and visually check both native-size and nearest-neighbor enlarged previews. Inspect the PNG after saving it; don't assume a generated image already matches Minecraft requirements.
9. Update required model JSON, blockstates, atlas/animation metadata (`.png.mcmeta`), localization or other references **only when the asset actually requires them**. Preserve Minecraft's resource path hierarchy.
10. Commit the **final native-size texture and associated source changes to GitHub** when repository access is available. Run the project's NeoForge CI/build where applicable and check the result. Show the user the commit and tested outcome; a standalone illustration does not complete the task.

## Resource paths and common checks

- Item sprites: `src/main/resources/assets/beyond_the_horizons/textures/item/<name>.png`
- Item models: `src/main/resources/assets/beyond_the_horizons/models/item/<name>.json`
- Block faces: `src/main/resources/assets/beyond_the_horizons/textures/block/<name>.png`
- Block models/states: `models/block/` and `blockstates/` within the same namespace.
- Confirm that the model's texture references are correct; do not inadvertently point a new asset to a temporary Ad Astra or vanilla icon.
- Validate PNG size, mode/alpha, and actual visible silhouette with Pillow or equivalent tools.
- The current project's build workflow: `.github/workflows/build.yml` (`./gradlew neoforgeIncludeJar`).

## Code and documentation behavior

- Preserve Java 21 and the current NeoForge architecture; do not silently modify Fabric or future versions when working on NeoForge.
- Use conventional Java braces even for short conditional branches.
- Keep JSON legible and indented rather than collapsing entire objects onto one line.
- Do not change recipes, gameplay balance, progression, or texture dimensions beyond the requested scope.
- If the game appearance still needs verification, report CI success separately from an actual in-game visual test.
