package dev.ludwici.beyondthehorizons.neoforge.items;

import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Comparator;

public final class BeyondTheHorizonsCreativeTabs {

    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BeyondTheHorizons.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.beyond_the_horizons"))
                    .icon(() -> new ItemStack(DivingSuitItems.EXPEDITION_CHESTPLATE.get()))
                    .displayItems((parameters, output) -> BuiltInRegistries.ITEM.stream()
                            .filter(item -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(BeyondTheHorizons.MOD_ID))
                            .sorted(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString()))
                            .forEach(item -> output.accept(item)))
                    .build());

    private BeyondTheHorizonsCreativeTabs() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
