package dev.ludwici.beyondthehorizons.neoforge.environment;

import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class EnvironmentAttributes {

    private static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, BeyondTheHorizons.MOD_ID);

    public static final DeferredHolder<Attribute, Attribute> PRESSURE_RESISTANCE =
            ATTRIBUTES.register("pressure_resistance",
                    () -> new RangedAttribute("attribute.name.beyond_the_horizons.pressure_resistance", 4.5, 0.0, 256.0)
                            .setSyncable(true));

    private EnvironmentAttributes() {
    }

    public static void register(IEventBus modBus) {
        ATTRIBUTES.register(modBus);
        modBus.addListener(EnvironmentAttributes::onAttributeModification);
    }

    private static void onAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, PRESSURE_RESISTANCE);
    }
}
