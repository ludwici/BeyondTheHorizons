package dev.ludwici.beyondthehorizons.neoforge;

import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import dev.ludwici.beyondthehorizons.neoforge.worldgen.NeptuneWorldgen;
import earth.terrarium.adastra.api.events.AdAstraEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(BeyondTheHorizons.MOD_ID)
public final class BeyondTheHorizonsNeoForge {

    private static final ResourceKey<Level> NEPTUNE = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(BeyondTheHorizons.MOD_ID, "neptune")
    );

    public BeyondTheHorizonsNeoForge(IEventBus modBus) {
        NeptuneWorldgen.register(modBus);
        AdAstraEvents.EnvironmentTickEvent.register((level, pos, state, temperature) -> {
            if (!level.dimension().equals(NEPTUNE)) {
                return true;
            }

            return !state.getFluidState().is(FluidTags.WATER);
        });
        BeyondTheHorizons.init();
    }
}
