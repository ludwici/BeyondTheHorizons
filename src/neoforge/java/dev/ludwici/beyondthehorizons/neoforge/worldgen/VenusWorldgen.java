package dev.ludwici.beyondthehorizons.neoforge.worldgen;

import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class VenusWorldgen {

    private static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, BeyondTheHorizons.MOD_ID);

    public static final DeferredHolder<Feature<?>, VenusAnhydriteFeature> ANHYDRITE_CRUST =
            FEATURES.register("venus_anhydrite_crust",
                    () -> new VenusAnhydriteFeature(NoneFeatureConfiguration.CODEC));

    private VenusWorldgen() {
    }

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
