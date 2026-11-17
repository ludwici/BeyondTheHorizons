# Environmental System — first implementation

> NeoForge 1.21.1. This is the initial playable environmental-pressure implementation, not a final balance or support for atmospheric gas giants.

## Available now

- `EnvironmentSampler` resolves a registered dimension provider into `EnvironmentSnapshot` containing pressure, Ad Astra temperature, future wind/radiation fields, and a pressure-hazard indicator.
- `NeptuneEnvironment` provides underwater pressure and flags `beyond_the_horizons:pressure_abyss` as dangerous below Y=40. All other dimensions default to safe 1 bar; no artificial hazards on unrelated worlds.
- Pressure uses **a simplified game approximation**: 1 bar at nominal ocean boundary Y=85, increasing by 0.1 bar per block below, including unprotected air pockets in the abyss. The real generated water/ice boundary varies around this nominal value. Wind and radiation are currently 0 placeholders, not implemented gameplay.
- Players have `beyond_the_horizons:pressure_resistance`, a synced Minecraft attribute in bar (default **4.5**). This is an extension point for future suits.
- When inside Pressure Abyss **and** surrounding pressure exceeds resistance, the player sees a warning. Simply placing air at depth is not an alternative to pressure protection. After **3 seconds** of uninterrupted exposure, pressure causes damage each second, scaling with overload. Leaving the water/hazard area or gaining enough resistance resets the exposure. Creative/spectator players are unaffected.
- Damage type `beyond_the_horizons:pressure` bypasses normal armor; future proper resistance, sealed vehicles and protective equipment are the intended defense.
- `PressureProtection.SealedVehicle` is an interface future sealed bathyscaphe entities can implement to supply a pressure-resistance limit. No vehicle or suit is added in this change.

## Commands for operators

- `/bth environment debug` toggles an actionbar diagnostic display for the executing player: pressure, resistance, temperature, wind, radiation and whether the location is Pressure Abyss.
- `/bth environment inspect` prints a one-off reading.
- `/attribute @s beyond_the_horizons:pressure_resistance base set 30` simulates future protective equipment; use `base set 4.5` to restore the initial value. These are vanilla attribute commands, available to operators.

## Suggested testing

1. Create a **new Neptune world** to ensure the biome and water generation exist. Enable cheats or operator status.
2. Run `/bth environment debug` and check the overlay on the ice surface (1 bar), in the ocean (increasing pressure), and in Pressure Abyss below Y=40.
3. With the default attribute, enter Pressure Abyss: warning immediately; damage after about 3 seconds, fatal with prolonged exposure. Check that an ordinary air pocket at depth remains hazardous.
4. Swim back into normal ocean or leave the abyss biome: damage and warning should stop; short reentry must restart the grace period.
5. Run `/attribute @s beyond_the_horizons:pressure_resistance base set 30`: confirm that the danger disappears and the debug indicator shows increased protection. Then restore the attribute to 4.5.
6. Check creative/spectator immunity, deaths and respawns, changing dimensions, survival without water, and server restart.
7. Confirm no changes to Ad Astra oxygen mechanics or the existing prevention of random freezing in Neptune's ocean.

## Future work

- Move balance settings and environment profiles into data-driven definitions.
- Add the first diving suit with attribute modifiers; later, a sealed bathyscaphe.
- Add different planet profiles for Jupiter, Saturn and Uranus, including actual wind/radiation effects.
- Use accurate local ice/water boundary depth instead of the nominal water height if desired.
- Consider a dedicated HUD once there are useful non-debug environmental instruments.
