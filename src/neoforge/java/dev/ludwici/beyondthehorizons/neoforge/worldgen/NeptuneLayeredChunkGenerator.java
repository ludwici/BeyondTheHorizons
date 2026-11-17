package dev.ludwici.beyondthehorizons.neoforge.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.StructureSet;

/**
 * Vanilla flat layers and fluid handling, but with a 3D biome source.
 */
public final class NeptuneLayeredChunkGenerator extends ChunkGenerator {

    public static final MapCodec<NeptuneLayeredChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource),
            FlatLevelGeneratorSettings.CODEC.fieldOf("settings").forGetter(generator -> generator.settings)
    ).apply(instance, NeptuneLayeredChunkGenerator::new));

    private final FlatLevelGeneratorSettings settings;
    private final FlatLevelSource flat;

    public NeptuneLayeredChunkGenerator(BiomeSource biomeSource, FlatLevelGeneratorSettings settings) {
        super(biomeSource, settings::adjustGenerationSettings);
        this.settings = settings;
        this.flat = new FlatLevelSource(settings);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structures, RandomState random, long seed) {
        return flat.createState(structures, random, seed);
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager manager,
                             StructureManager structures, ChunkAccess chunk, GenerationStep.Carving step) {
        flat.applyCarvers(level, seed, random, manager, structures, chunk, step);
    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structures, RandomState random, ChunkAccess chunk) {
        flat.buildSurface(level, structures, random, chunk);
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState random, StructureManager structures,
                                                         ChunkAccess chunk) {
        return flat.fillFromNoise(blender, random, structures, chunk);
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor height, RandomState random) {
        return flat.getBaseColumn(x, z, height, random);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return flat.getBaseHeight(x, z, type, level, random);
    }

    @Override
    public int getGenDepth() {
        return flat.getGenDepth();
    }

    @Override
    public int getMinY() {
        return flat.getMinY();
    }

    @Override
    public int getSeaLevel() {
        return flat.getSeaLevel();
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor level) {
        return flat.getSpawnHeight(level);
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
        flat.spawnOriginalMobs(level);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        flat.addDebugScreenInfo(info, random, pos);
    }
}
