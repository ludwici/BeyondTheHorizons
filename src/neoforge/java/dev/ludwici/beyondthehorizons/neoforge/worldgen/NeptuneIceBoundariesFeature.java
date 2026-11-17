package dev.ludwici.beyondthehorizons.neoforge.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class NeptuneIceBoundariesFeature extends Feature<NoneFeatureConfiguration> {

    private static final int WATER_ICE_BASE_Y = 85;
    private static final int BLUE_PACKED_BASE_Y = 155;
    private static final int SURFACE_BASE_Y = 184;

    private static final int WATER_ICE_VARIATION = 16;
    private static final int BLUE_PACKED_VARIATION = 13;
    private static final int SURFACE_VARIATION = 13;
    private static final int MIN_SURFACE_PACKED_ICE = 12;

    private static final long LOWER_BOUNDARY_SALT = 0x4e455054554e45L;
    private static final long UPPER_BOUNDARY_SALT = 0x4943455348454c4cL;
    private static final long SURFACE_SALT = 0x53555246414345L;

    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState BLUE_ICE = Blocks.BLUE_ICE.defaultBlockState();
    private static final BlockState PACKED_ICE = Blocks.PACKED_ICE.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState SNOW = Blocks.SNOW_BLOCK.defaultBlockState();
    private static final BlockState POWDER_SNOW = Blocks.POWDER_SNOW.defaultBlockState();
    private static final BlockState DEEPSLATE = Blocks.DEEPSLATE.defaultBlockState();
    private static final BlockState BASALT = Blocks.BASALT.defaultBlockState();
    private static final BlockState TUFF = Blocks.TUFF.defaultBlockState();
    private static final BlockState AMETHYST = Blocks.AMETHYST_BLOCK.defaultBlockState();

    public NeptuneIceBoundariesFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        var origin = context.origin();
        long seed = level.getSeed();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = 0; dx < 16; dx++) {
            int x = origin.getX() + dx;

            for (int dz = 0; dz < 16; dz++) {
                int z = origin.getZ() + dz;
                boolean methaneDrifts = NeptuneBiomeSource.isMethaneDrift(x, z);
                boolean pressureAbyss = NeptuneBiomeSource.isPressureAbyss(x, z);

                double lowerNoise = terrainNoise(seed, x, z, LOWER_BOUNDARY_SALT);
                int waterIceBoundary = WATER_ICE_BASE_Y + (int) Math.round(lowerNoise * WATER_ICE_VARIATION);

                double surfaceMacro = sample(seed ^ SURFACE_SALT, x / 120.0, z / 120.0);
                double surfaceMedium = sample(seed ^ (SURFACE_SALT * 31), x / 52.0, z / 52.0);
                double surfaceFine = sample(seed ^ (SURFACE_SALT * 131), x / 19.0, z / 19.0);
                int surfaceOffset = (int) Math.round(surfaceMacro * 14 + surfaceMedium * 6 + surfaceFine * 2);
                int surfaceY = SURFACE_BASE_Y + Math.clamp(surfaceOffset, -SURFACE_VARIATION, SURFACE_VARIATION);

                double upperMacro = sample(seed ^ UPPER_BOUNDARY_SALT, x / 76.0, z / 76.0);
                double upperMedium = sample(seed ^ (UPPER_BOUNDARY_SALT * 31), x / 29.0, z / 29.0);
                double upperFine = sample(seed ^ (UPPER_BOUNDARY_SALT * 131), x / 12.0, z / 12.0);
                int upperOffset = (int) Math.round(upperMacro * 10 + upperMedium * 5 + upperFine * 2);
                int bluePackedBoundary = BLUE_PACKED_BASE_Y + Math.clamp(upperOffset, -BLUE_PACKED_VARIATION, BLUE_PACKED_VARIATION);
                bluePackedBoundary = Math.min(bluePackedBoundary, surfaceY - MIN_SURFACE_PACKED_ICE);

                for (int y = WATER_ICE_BASE_Y - WATER_ICE_VARIATION; y <= WATER_ICE_BASE_Y + WATER_ICE_VARIATION; y++) {
                    pos.set(x, y, z);
                    BlockState current = level.getBlockState(pos);
                    if (!current.is(Blocks.WATER) && !current.is(Blocks.BLUE_ICE)) {
                        continue;
                    }

                    BlockState target = y < waterIceBoundary ? WATER : BLUE_ICE;
                    if (current != target) {
                        level.setBlock(pos, target, 2);
                    }
                }

                for (int y = BLUE_PACKED_BASE_Y - BLUE_PACKED_VARIATION; y <= BLUE_PACKED_BASE_Y + BLUE_PACKED_VARIATION; y++) {
                    pos.set(x, y, z);
                    BlockState current = level.getBlockState(pos);
                    if (!current.is(Blocks.BLUE_ICE) && !current.is(Blocks.PACKED_ICE)) {
                        continue;
                    }

                    BlockState target = y < bluePackedBoundary ? BLUE_ICE : PACKED_ICE;
                    if (current != target) {
                        level.setBlock(pos, target, 2);
                    }
                }

                for (int y = SURFACE_BASE_Y - SURFACE_VARIATION + 1; y <= SURFACE_BASE_Y + SURFACE_VARIATION; y++) {
                    pos.set(x, y, z);
                    BlockState current = level.getBlockState(pos);
                    if (!current.isAir() && !current.is(Blocks.PACKED_ICE)) {
                        continue;
                    }

                    BlockState target = y <= surfaceY ? PACKED_ICE : AIR;
                    if (current != target) {
                        level.setBlock(pos, target, 2);
                    }
                }

                if (methaneDrifts) {
                    double snowNoise = terrainNoise(seed, x, z, 0x534E4F5744524946L);
                    int snowDepth = 1 + Math.round((float) Math.max(0, snowNoise * 3.0 + 1.0));

                    for (int y = surfaceY - snowDepth + 1; y <= surfaceY; y++) {
                        pos.set(x, y, z);
                        if (level.getBlockState(pos).is(Blocks.PACKED_ICE)) {
                            level.setBlock(pos, SNOW, 2);
                        }
                    }

                    if (snowDepth >= 3 && snowNoise > 0.65) {
                        pos.set(x, surfaceY, z);
                        if (level.getBlockState(pos).is(Blocks.SNOW_BLOCK)) {
                            level.setBlock(pos, POWDER_SNOW, 2);
                        }
                    }
                } else {
                    double iceVeinNoise = terrainNoise(seed, x, z, 0x4943455645494E53L);
                    if (iceVeinNoise > 0.64) {
                        pos.set(x, surfaceY, z);
                        if (level.getBlockState(pos).is(Blocks.PACKED_ICE)) {
                            level.setBlock(pos, BLUE_ICE, 2);
                        }
                    }
                }

                if (pressureAbyss) {
                    double floorNoise = terrainNoise(seed, x, z, 0x4142595353464C52L);
                    int floorHeight = Math.clamp(3 + Math.round((float) (floorNoise * 3)), 1, 6);
                    BlockState floorBlock = floorNoise > 0.3 ? BASALT : floorNoise < -0.35 ? TUFF : DEEPSLATE;

                    for (int y = -59; y < -59 + floorHeight; y++) {
                        pos.set(x, y, z);
                        if (level.getBlockState(pos).is(Blocks.WATER)) {
                            level.setBlock(pos, floorBlock, 2);
                        }
                    }

                    if (floorNoise > 0.7) {
                        pos.set(x, -59 + floorHeight - 1, z);
                        if (level.getBlockState(pos).is(Blocks.BASALT)) {
                            level.setBlock(pos, AMETHYST, 2);
                        }
                    }
                }
            }
        }

        return true;
    }

    private static double terrainNoise(long seed, int x, int z, long salt) {
        double broad = sample(seed ^ salt, x / 80.0, z / 80.0);
        double detail = sample(seed ^ (salt * 31), x / 28.0, z / 28.0);
        return broad * 0.8 + detail * 0.2;
    }

    private static double sample(long seed, double x, double z) {
        long ix = (long) Math.floor(x);
        long iz = (long) Math.floor(z);
        double fx = smooth(x - ix);
        double fz = smooth(z - iz);

        double south = lerp(hash(seed, ix, iz), hash(seed, ix + 1, iz), fx);
        double north = lerp(hash(seed, ix, iz + 1), hash(seed, ix + 1, iz + 1), fx);
        return lerp(south, north, fz);
    }

    private static double smooth(double value) {
        return value * value * (3.0 - 2.0 * value);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static double hash(long seed, long x, long z) {
        long value = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (z * 0xC2B2AE3D27D4EB4FL);
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        value ^= value >>> 33;
        return ((value >>> 11) * 0x1.0p-53) * 2.0 - 1.0;
    }
}
