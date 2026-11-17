package dev.ludwici.beyondthehorizons.neoforge.environment;

import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public final class NeptuneEnvironment {

    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(BeyondTheHorizons.MOD_ID, "neptune")
    );

    private static final ResourceKey<Biome> PRESSURE_ABYSS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(BeyondTheHorizons.MOD_ID, "pressure_abyss")
    );

    private static final int NOMINAL_OCEAN_SURFACE_Y = 85;
    private static final int ABYSS_START_Y = 40;
    private static final double BARS_PER_BLOCK = 0.1;

    private NeptuneEnvironment() {
    }

    public static EnvironmentSnapshot sample(ServerLevel level, BlockPos pos, short temperature) {
        boolean submerged = level.getFluidState(pos).is(FluidTags.WATER);
        double pressure = submerged
                ? 1.0 + Math.max(0, NOMINAL_OCEAN_SURFACE_Y - pos.getY()) * BARS_PER_BLOCK
                : 1.0;
        boolean abyss = submerged && pos.getY() < ABYSS_START_Y && level.getBiome(pos).is(PRESSURE_ABYSS);

        return new EnvironmentSnapshot(pressure, temperature, 0.0, 0.0, abyss);
    }
}
