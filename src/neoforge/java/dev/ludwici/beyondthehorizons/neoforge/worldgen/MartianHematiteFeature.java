
package dev.ludwici.beyondthehorizons.neoforge.worldgen;

import com.mojang.serialization.Codec;
import dev.ludwici.beyondthehorizons.neoforge.mars.MartianHematiteRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Heightmap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class MartianHematiteFeature extends Feature<NoneFeatureConfiguration> {

    private static final TagKey<Block> REPLACEABLE =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(
                    "beyond_the_horizons", "hematite_strata_replaceables"));

    public MartianHematiteFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        var random = context.random();
        BlockPos origin = context.origin();
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        int centerY = surfaceY - 5 - random.nextInt(9);
        int halfLength = 7 + random.nextInt(5);
        int halfWidth = 2 + random.nextInt(2);
        int depth = 1 + random.nextInt(2);
        boolean alongX = random.nextBoolean();
        double slope = (random.nextDouble() - 0.5) * 0.24;
        int placed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int along = -halfLength; along <= halfLength; along++) {
            for (int across = -halfWidth; across <= halfWidth; across++) {
                double distance = (double) (along * along) / (halfLength * halfLength)
                        + (double) (across * across) / (halfWidth * halfWidth);
                if (distance > 1.0 || random.nextFloat() < 0.08f) {
                    continue;
                }

                int x = origin.getX() + (alongX ? along : across);
                int z = origin.getZ() + (alongX ? across : along);
                int y = centerY + (int) Math.round(along * slope);
                for (int dy = 0; dy < depth; dy++) {
                    int worldY = y + dy;
                    if (worldY <= level.getMinBuildHeight() + 5
                            || worldY >= level.getMaxBuildHeight() - 5) {
                        continue;
                    }

                    pos.set(x, worldY, z);
                    if (level.getBlockState(pos).is(REPLACEABLE)) {
                        level.setBlock(pos, MartianHematiteRegistry.HEMATITE_STRATUM.get().defaultBlockState(), 2);
                        placed++;
                    }
                }
            }
        }

        return placed > 0;
    }
}
