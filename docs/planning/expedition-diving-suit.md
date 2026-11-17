# Expedition Diving Suit — initial NeoForge implementation

> First basic diving suit. NeoForge 1.21.1 only; upgraded suits and bathyscaphe remain on the roadmap.

- Four armor pieces: `expedition_diving_helmet`, `expedition_diving_chestplate`, `expedition_diving_leggings`, `expedition_diving_boots`.
- Crafted with Ad Astra steel plates and Desh; the chest also needs an Ad Astra gas tank. Items appear in the Combat creative tab.
- A **complete, matching four-piece set** increases effective pressure resistance by **3 bar**. Default 4.5 bar plus the suit gives 7.5 bar.
- Protection is computed on the server from actually worn equipment on every environment evaluation, so removing any piece immediately disables the bonus. The base `pressure_resistance` attribute remains available for operator testing and future equipment.
- The chest uses Ad Astra's `SpaceSuitItem` oxygen storage and underwater breathing logic; fill it with the existing Ad Astra oxygen-loading mechanisms. This avoids introducing a second oxygen storage mechanic.
- The suit currently uses Ad Astra space-suit visual assets as a **temporary appearance**. Custom underwater gear art can be introduced later; all four items have their own IDs and localization.

## Abyss visuals

- Pressure Abyss biome now has even darker water/fog tint.
- Client-side `ViewportEvent.ComputeFogColor` overrides the final water fog color in that biome, with a deeper blue-black shade as depth increases.
- Client-side `ViewportEvent.RenderFog` reduces underwater visibility to roughly 9–14 blocks at depth. **The effect is only active underwater on Neptune in Pressure Abyss**, not in Neptunian Ocean, and should never affect other planets.
- The additional fog fades in and out gradually (about 5 seconds to become nearly complete), instead of snapping to a new color and distance on crossing a biome boundary. Both color and near/far distances use the same blended intensity. The effect also increases progressively by depth across 16 blocks below Y=40, then reaches full strength.
- Behavior in shader packs may differ because shaders can replace vanilla fog, and requires in-game testing.

## Test checklist

1. Verify all four suit pieces craft from existing Ad Astra components and are visible in the separate **Ad Astra: Beyond The Horizons** creative tab, not the Combat tab.
2. Use `/bth environment debug` in Survival. Equip three pieces and then the fourth; effective protection should change from 4.5 to 7.5 bar immediately.
3. Enter a shallow edge of Pressure Abyss; survive while ambient pressure is **at or below** 7.5 bar. Descend until it exceeds 7.5 bar and confirm warning, grace period, and damage.
4. Remove any single piece and observe the bonus disappearing.
5. Fill the chest item with oxygen using Ad Astra, submerge and ensure underwater air replenishes as expected. Check empty oxygen reserves, death/respawn, and world reload.
6. Compare the underwater appearance in the Neptunian Ocean and Pressure Abyss. Swim or teleport across the border and back: darkness and visibility should transition smoothly over several seconds, including across the Y=40 boundary. Ordinary water, other dimensions and non-water views should remain unaffected.
7. Repeat the fog test with and without shaders, documenting differences.
