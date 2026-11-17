package dev.ludwici.beyondthehorizons.neoforge.environment;

import earth.terrarium.adastra.api.systems.TemperatureApi;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public final class EnvironmentSampler {

    @FunctionalInterface
    public interface Provider {
        EnvironmentSnapshot sample(ServerLevel level, BlockPos pos, short temperature);
    }

    private static final Map<ResourceKey<Level>, Provider> PROVIDERS = new HashMap<>();

    static {
        register(NeptuneEnvironment.DIMENSION, NeptuneEnvironment::sample);
    }

    private EnvironmentSampler() {
    }

    public static void register(ResourceKey<Level> dimension, Provider provider) {
        PROVIDERS.put(dimension, provider);
    }

    public static boolean hasProvider(ResourceKey<Level> dimension) {
        return PROVIDERS.containsKey(dimension);
    }

    public static EnvironmentSnapshot sample(ServerLevel level, BlockPos pos) {
        short temperature = TemperatureApi.API.getTemperature(level, pos);
        Provider provider = PROVIDERS.get(level.dimension());
        if (provider == null) {
            return new EnvironmentSnapshot(1.0, temperature, 0.0, 0.0, false);
        }

        return provider.sample(level, pos, temperature);
    }
}
