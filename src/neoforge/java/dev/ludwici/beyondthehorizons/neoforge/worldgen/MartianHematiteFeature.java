package dev.ludwici.beyondthehorizons.neoforge.worldgen;

import com.mojang.serialization.Codec;
import dev.ludwici.beyondthehorizons.neoforge.mars.MartianHematiteRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class MartianHematiteFeature extends Feature<NoneFeatureConfiguration> {

    private static final TagKey<Block> REPLACEABLE =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(
                    "beyond_the_horizons", "hematite_strata_replaceables"));

    private static final int CLIFF_SAMPLE_RADIUS = 6;
    private static final int CLIFF_HEIGHT_DIFFERENCE = 9;

    public MartianHematiteFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        var random = context.random();
        BlockPos origin = context.origin();
        int originX = origin.getX();
        int originZ = origin.getZ();
        int westHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG,
                originX - CLIFF_SAMPLE_RADIUS, originZ);
        int eastHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG,
                originX + CLIFF_SAMPLE_RADIUS, originZ);
        int northHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG,
                originX, originZ - CLIFF_SAMPLE_RADIUS);
        int southHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG,
                originX, originZ + CLIFF_SAMPLE_RADIUS);
        int deltaX = eastHeight - westHeight;
        int deltaZ = southHeight - northHeight;
        boolean cliff = Math.max(Math.abs(deltaX), Math.abs(deltaZ)) >= CLIFF_HEIGHT_DIFFERENCE;
        boolean alongX = cliff ? Math.abs(deltaX) < Math.abs(deltaZ) : random.nextBoolean();

        int centerX = originX;
        int centerZ = originZ;
        int centerY;
        if (cliff) {
            boolean steepX = Math.abs(deltaX) >= Math.abs(deltaZ);
            int highHeight = steepX ? Math.max(eastHeight, westHeight) : Math.max(southHeight, northHeight);
            int lowHeight = steepX ? Math.min(eastHeight, westHeight) : Math.min(southHeight, northHeight);
            if (steepX) {
                centerX += deltaX > 0 ? 2 : -2;
            } else {
                centerZ += deltaZ > 0 ? 2 : -2;
            }

            centerY = lowHeight + Math.min(6, (highHeight - lowHeight) / 2) - random.nextInt(2);
        } else {
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, originX, originZ);
            int cover = random.nextFloat() < 0.16f ? 2 + random.nextInt(3) : 7 + random.nextInt(8);
            centerY = surfaceY - cover;
        }

        int halfLength = 8 + random.nextInt(5);
        int halfWidth = 2 + random.nextInt(2);
        double slope = (random.nextDouble() - 0.5) * 0.22;
        double phase = random.nextDouble() * Math.PI * 2.0;
        double curve = (random.nextDouble() - 0.5) * 1.8;
        boolean secondaryLayer = random.nextFloat() < 0.35f;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int placed = 0;

        for (int layer = 0; layer < (secondaryLayer ? 2 : 1); layer++) {
            int layerLength = layer == 0 ? halfLength : Math.max(4, halfLength - 3);
            int layerWidth = layer == 0 ? halfWidth : Math.max(1, halfWidth - 1);
            int layerY = centerY - layer * (2 + random.nextInt(2));
            double layerPhase = phase + layer * 0.65;
            double layerOffset = layer == 0 ? 0.0 : (random.nextBoolean() ? 1.0 : -1.0);

            for (int along = -layerLength; along <= layerLength; along++) {
                double lengthFraction = (double) along / layerLength;
                double bend = Math.sin(along * 0.24 + layerPhase) * curve + layerOffset;
                double widthVariation = 0.85 + 0.18 * Math.sin(along * 0.56 + layerPhase * 1.3);
                double taper = Math.sqrt(Math.max(0.0, 1.0 - lengthFraction * lengthFraction));
                double availableWidth = layerWidth * taper * widthVariation;

                for (int across = -layerWidth - 2; across <= layerWidth + 2; across++) {
                    double edgeDistance = Math.abs(across - bend);
                    if (edgeDistance > availableWidth) {
                        continue;
                    }

                    boolean nearEdge = availableWidth - edgeDistance < 0.9;
                    if (nearEdge && random.nextFloat() < 0.26f) {
                        continue;
                    }

                    if (random.nextFloat() < (layer == 0 ? 0.035f : 0.12f)) {
                        continue;
                    }

                    int x = centerX + (alongX ? along : across);
                    int z = centerZ + (alongX ? across : along);
                    int y = layerY + (int) Math.round(along * slope
                            + 0.7 * Math.sin(along * 0.29 + layerPhase));
                    int thickness = random.nextFloat() < 0.20f ? 2 : 1;

                    for (int dy = 0; dy < thickness; dy++) {
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
        }

        return placed > 0;
    }
}
