package dev.ludwici.beyondthehorizons.neoforge.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

public final class NeptuneBiomeSource extends BiomeSource {

    public static final MapCodec<NeptuneBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Biome.CODEC.fieldOf("cobalt_plains").forGetter(source -> source.cobaltPlains),
            Biome.CODEC.fieldOf("methane_drifts").forGetter(source -> source.methaneDrifts),
            Biome.CODEC.fieldOf("neptunian_ocean").forGetter(source -> source.neptunianOcean),
            Biome.CODEC.fieldOf("pressure_abyss").forGetter(source -> source.pressureAbyss)
    ).apply(instance, NeptuneBiomeSource::new));

    private static final long METHANE_SALT = 0x4D455448414E45L;
    private static final long ABYSS_SALT = 0x41425953535F4EL;

    private final Holder<Biome> cobaltPlains;
    private final Holder<Biome> methaneDrifts;
    private final Holder<Biome> neptunianOcean;
    private final Holder<Biome> pressureAbyss;

    public NeptuneBiomeSource(Holder<Biome> cobaltPlains, Holder<Biome> methaneDrifts,
                              Holder<Biome> neptunianOcean, Holder<Biome> pressureAbyss) {
        this.cobaltPlains = cobaltPlains;
        this.methaneDrifts = methaneDrifts;
        this.neptunianOcean = neptunianOcean;
        this.pressureAbyss = pressureAbyss;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(cobaltPlains, methaneDrifts, neptunianOcean, pressureAbyss);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        int blockX = x * 4;
        int blockY = y * 4;
        int blockZ = z * 4;

        if (blockY < 40) {
            return isPressureAbyss(blockX, blockZ) ? pressureAbyss : neptunianOcean;
        }
        if (blockY < 92) {
            return neptunianOcean;
        }
        return isMethaneDrift(blockX, blockZ) ? methaneDrifts : cobaltPlains;
    }

    public static boolean isMethaneDrift(int x, int z) {
        double broad = sample(x / 220.0, z / 220.0, METHANE_SALT);
        double medium = sample(x / 92.0, z / 92.0, METHANE_SALT * 31);
        return broad * 0.8 + medium * 0.2 > 0.08;
    }

    public static boolean isPressureAbyss(int x, int z) {
        double broad = sample(x / 320.0, z / 320.0, ABYSS_SALT);
        double medium = sample(x / 130.0, z / 130.0, ABYSS_SALT * 31);
        return broad * 0.85 + medium * 0.15 < -0.25;
    }

    private static double sample(double x, double z, long seed) {
        long bx = (long) Math.floor(x);
        long bz = (long) Math.floor(z);
        double fx = smooth(x - bx);
        double fz = smooth(z - bz);
        double a = lerp(hash(seed, bx, bz), hash(seed, bx + 1, bz), fx);
        double b = lerp(hash(seed, bx, bz + 1), hash(seed, bx + 1, bz + 1), fx);
        return lerp(a, b, fz);
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
