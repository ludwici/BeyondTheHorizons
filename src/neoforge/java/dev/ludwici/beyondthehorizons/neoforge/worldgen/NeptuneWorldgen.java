package dev.ludwici.beyondthehorizons.neoforge.worldgen;

import com.mojang.serialization.MapCodec;
import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeptuneWorldgen {

    private static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, BeyondTheHorizons.MOD_ID);

    public static final DeferredHolder<Feature<?>, NeptuneIceBoundariesFeature> NEPTUNE_ICE_BOUNDARIES =
            FEATURES.register("neptune_ice_boundaries", () -> new NeptuneIceBoundariesFeature(NoneFeatureConfiguration.CODEC));

    private static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, BeyondTheHorizons.MOD_ID);

    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, BeyondTheHorizons.MOD_ID);

    static {
        BIOME_SOURCES.register("neptune", () -> NeptuneBiomeSource.CODEC);
        CHUNK_GENERATORS.register("neptune_layered", () -> NeptuneLayeredChunkGenerator.CODEC);
    }

    private NeptuneWorldgen() {
    }

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
        BIOME_SOURCES.register(modBus);
        CHUNK_GENERATORS.register(modBus);
    }
}
