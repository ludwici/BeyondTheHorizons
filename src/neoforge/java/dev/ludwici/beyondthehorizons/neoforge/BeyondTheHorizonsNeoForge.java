package dev.ludwici.beyondthehorizons.neoforge;

import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import dev.ludwici.beyondthehorizons.neoforge.worldgen.NeptuneWorldgen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(BeyondTheHorizons.MOD_ID)
public final class BeyondTheHorizonsNeoForge {

    public BeyondTheHorizonsNeoForge(IEventBus modBus) {
        NeptuneWorldgen.register(modBus);
        BeyondTheHorizons.init();
    }
}
