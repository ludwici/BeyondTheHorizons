package dev.ludwici.beyondthehorizons.neoforge.worldgen;

import com.mojang.serialization.Codec;
import dev.ludwici.beyondthehorizons.neoforge.venus.AnhydriteCrustBlock;
import dev.ludwici.beyondthehorizons.neoforge.venus.VenusAnhydriteRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

    private static final Direction[] EXPOSED_FACES = {
            Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    public VenusAnhydriteFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        var random = context.random();
        BlockPos origin = context.origin();
        int radiusX = 2 + random.nextInt(3);
        int radiusZ = 2 + random.nextInt(3);
        BlockPos.MutableBlockPos rockPos = new BlockPos.MutableBlockPos();
        int placed = 0;

        for (int offsetX = -radiusX; offsetX <= radiusX; offsetX++) {
            for (int offsetZ = -radiusZ; offsetZ <= radiusZ; offsetZ++) {
                double distance = (double) (offsetX * offsetX) / (radiusX * radiusX)
                        + (double) (offsetZ * offsetZ) / (radiusZ * radiusZ);
                if (distance > 1.0 || random.nextFloat() < 0.18f) {
                    continue;
                }

                int x = origin.getX() + offsetX;
                int z = origin.getZ() + offsetZ;
                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                for (int below = 0; below <= 6; below++) {
                    int y = surfaceY - below;
                    if (y <= level.getMinBuildHeight() + 4 || y >= level.getMaxBuildHeight() - 4) {
                        continue;
                    }

                    rockPos.set(x, y, z);
                    if (!level.getBlockState(rockPos).is(HOSTS)) {
                        continue;
                    }

                    Direction face = findExposedFace(level, rockPos);
                    if (face == null) {
                        continue;
                    }

                    BlockPos crustPos = rockPos.relative(face);
                    level.setBlock(crustPos, VenusAnhydriteRegistry.ANHYDRITE_CRUST.get()
                            .defaultBlockState().setValue(AnhydriteCrustBlock.FACING, face), 2);
                    placed++;
                    break;
                }
            }
        }

        return placed > 0;
    }

    private static Direction findExposedFace(net.minecraft.world.level.WorldGenLevel level, BlockPos rockPos) {
        for (Direction face : EXPOSED_FACES) {
            if (level.isEmptyBlock(rockPos.relative(face))) {
                return face;
            }
        }

        return null;
    }
}
