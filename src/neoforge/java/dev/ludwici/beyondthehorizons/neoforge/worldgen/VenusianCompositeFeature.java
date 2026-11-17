package dev.ludwici.beyondthehorizons.neoforge.worldgen;

import com.mojang.serialization.Codec;
import dev.ludwici.beyondthehorizons.neoforge.venus.VenusianCompositeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class VenusianCompositeFeature extends Feature<NoneFeatureConfiguration> {

    private static final TagKey<Block> HOSTS = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("beyond_the_horizons", "venusian_composite_hosts"));

    public VenusianCompositeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        var random = context.random();
        BlockPos origin = context.origin();
        int x = origin.getX();
        int z = origin.getZ();
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int centerY = Integer.MIN_VALUE;

        for (int depth = 1; depth <= 11; depth++) {
            int y = surface - depth;
            if (y <= level.getMinBuildHeight() + 5) {
                break;
            }

            pos.set(x, y, z);
            if (level.getBlockState(pos).is(HOSTS)) {
                centerY = y;
                break;
            }
        }

        if (centerY == Integer.MIN_VALUE) {
            return false;
        }

        int radiusX = 2 + random.nextInt(2);
        int radiusZ = 2 + random.nextInt(2);
        int radiusY = 1 + random.nextInt(2);
        int count = 0;

        for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dy = -radiusY; dy <= radiusY; dy++) {
                for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                    double distance = (double) (dx * dx) / (radiusX * radiusX)
                            + (double) (dy * dy) / (radiusY * radiusY)
                            + (double) (dz * dz) / (radiusZ * radiusZ);
                    if (distance > 1.0 || random.nextFloat() < 0.25f) {
                        continue;
                    }

                    int y = centerY + dy;
                    if (y <= level.getMinBuildHeight() + 4 || y >= level.getMaxBuildHeight() - 4) {
                        continue;
                    }

                    pos.set(x + dx, y, z + dz);
                    if (level.getBlockState(pos).is(HOSTS)) {
                        level.setBlock(pos, VenusianCompositeRegistry.COMPOSITE.get().defaultBlockState(), 2);
                        count++;
                    }
                }
            }
        }

        return count > 0;
    }
}
