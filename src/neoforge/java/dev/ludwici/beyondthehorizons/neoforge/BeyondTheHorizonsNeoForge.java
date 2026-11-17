package dev.ludwici.beyondthehorizons.neoforge;

import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import dev.ludwici.beyondthehorizons.neoforge.environment.EnvironmentAttributes;
import dev.ludwici.beyondthehorizons.neoforge.environment.EnvironmentCommands;
import dev.ludwici.beyondthehorizons.neoforge.environment.EnvironmentEvents;
import dev.ludwici.beyondthehorizons.neoforge.environment.NeptuneEnvironment;
import dev.ludwici.beyondthehorizons.neoforge.items.BeyondTheHorizonsCreativeTabs;
import dev.ludwici.beyondthehorizons.neoforge.items.DivingSuitItems;
import dev.ludwici.beyondthehorizons.neoforge.worldgen.NeptuneWorldgen;
import earth.terrarium.adastra.api.events.AdAstraEvents;
import net.minecraft.tags.FluidTags;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(BeyondTheHorizons.MOD_ID)
public final class BeyondTheHorizonsNeoForge {

    public BeyondTheHorizonsNeoForge(IEventBus modBus) {
        NeptuneWorldgen.register(modBus);
        EnvironmentAttributes.register(modBus);
        DivingSuitItems.register(modBus);
        BeyondTheHorizonsCreativeTabs.register(modBus);
        NeoForge.EVENT_BUS.addListener(EnvironmentEvents::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(EnvironmentEvents::onPlayerLogout);
        NeoForge.EVENT_BUS.addListener(EnvironmentEvents::onPlayerRespawn);
        NeoForge.EVENT_BUS.addListener(EnvironmentEvents::onPlayerChangedDimension);
        NeoForge.EVENT_BUS.addListener(EnvironmentCommands::onRegisterCommands);
        AdAstraEvents.EnvironmentTickEvent.register((level, pos, state, temperature) -> {
            if (!level.dimension().equals(NeptuneEnvironment.DIMENSION)) {
                return true;
            }

            return !state.getFluidState().is(FluidTags.WATER);
        });
        BeyondTheHorizons.init();
    }
}
