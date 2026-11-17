package dev.ludwici.beyondthehorizons.neoforge.worldgen;

import com.mojang.serialization.Codec;
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

import java.util.ArrayList;
import java.util.List;

public final class VenusAnhydriteFeature extends Feature<NoneFeatureConfiguration> {

    private static final TagKey<Block> HOSTS = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("beyond_the_horizons", "anhydrite_crust_hosts"));
    private static final Direction[] DIRECTIONS = Direction.values();

    public VenusAnhydriteFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        var random = context.random();
        BlockPos origin = context.origin();
        int x = origin.getX();
        int z = origin.getZ();
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int firstRockY = Integer.MIN_VALUE;

        for (int depth = 0; depth <= 14; depth++) {
            int y = surfaceY - depth;
            if (y <= level.getMinBuildHeight() + 4) {
                break;
            }

            cursor.set(x, y, z);
            if (level.getBlockState(cursor).is(HOSTS)) {
                firstRockY = y;
                break;
            }
        }

        if (firstRockY == Integer.MIN_VALUE) {
            return false;
        }

        int centerY = firstRockY - (2 + random.nextInt(4));
        cursor.set(x, centerY, z);
        if (!level.getBlockState(cursor).is(HOSTS)) {
            return false;
        }

        BlockPos center = cursor.immutable();
        List<BlockPos> placed = new ArrayList<>();
        level.setBlock(center, VenusAnhydriteRegistry.ANHYDRITE_CRUST.get().defaultBlockState(), 2);
        placed.add(center);

        int desired = 3 + random.nextInt(4);
        for (int attempt = 0; attempt < desired * 10 && placed.size() < desired; attempt++) {
            BlockPos parent = placed.get(random.nextInt(placed.size()));
            Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
            BlockPos candidate = parent.relative(direction);

            if (Math.abs(candidate.getX() - x) > 2
                    || Math.abs(candidate.getZ() - z) > 2
                    || Math.abs(candidate.getY() - centerY) > 1
                    || candidate.getY() <= level.getMinBuildHeight() + 4
                    || candidate.getY() >= level.getMaxBuildHeight() - 4) {
                continue;
            }

            if (!level.getBlockState(candidate).is(HOSTS)) {
                continue;
            }

            level.setBlock(candidate, VenusAnhydriteRegistry.ANHYDRITE_CRUST.get().defaultBlockState(), 2);
            placed.add(candidate);
        }

        return !placed.isEmpty();
    }
}
