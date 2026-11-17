package dev.ludwici.beyondthehorizons.neoforge.worldgen;

import com.mojang.serialization.Codec;
import dev.ludwici.beyondthehorizons.neoforge.venus.VenusAnhydriteRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class VenusAnhydriteFeature extends Feature<NoneFeatureConfiguration> {

    private static final TagKey<Block> HOSTS = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("beyond_the_horizons", "anhydrite_crust_hosts"));

    public VenusAnhydriteFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        var random = context.random();
        BlockPos origin = context.origin();
        int centerX = origin.getX();
        int centerZ = origin.getZ();
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, centerX, centerZ) - 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int exposedRockY = Integer.MIN_VALUE;

        for (int depth = 0; depth <= 11; depth++) {
            int y = surfaceY - depth;
            if (y <= level.getMinBuildHeight() + 4) {
                break;
            }

            pos.set(centerX, y, centerZ);
            if (level.getBlockState(pos).is(HOSTS)) {
                exposedRockY = y;
                break;
            }
        }

        if (exposedRockY == Integer.MIN_VALUE) {
            return false;
        }

        int radiusX = 2 + random.nextInt(2);
        int radiusZ = 2 + random.nextInt(2);
        int radiusY = 1 + random.nextInt(2);
        int centerY = exposedRockY - random.nextInt(2);
        int placed = 0;

        for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                for (int dy = -radiusY; dy <= radiusY; dy++) {
                    double distance = (double) (dx * dx) / (radiusX * radiusX)
                            + (double) (dy * dy) / (radiusY * radiusY)
                            + (double) (dz * dz) / (radiusZ * radiusZ);
                    if (distance > 1.0 || random.nextFloat() < 0.22f) {
                        continue;
                    }

                    int y = centerY + dy;
                    if (y <= level.getMinBuildHeight() + 4 || y >= level.getMaxBuildHeight() - 4) {
                        continue;
                    }

                    pos.set(centerX + dx, y, centerZ + dz);
                    if (level.getBlockState(pos).is(HOSTS)) {
                        level.setBlock(pos, VenusAnhydriteRegistry.ANHYDRITE_CRUST.get().defaultBlockState(), 2);
                        placed++;
                    }
                }
            }
        }

        return placed > 0;
    }
}
