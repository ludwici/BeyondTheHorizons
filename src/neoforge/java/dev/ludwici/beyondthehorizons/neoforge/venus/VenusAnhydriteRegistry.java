package dev.ludwici.beyondthehorizons.neoforge.venus;

import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class VenusAnhydriteRegistry {

    private static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(BeyondTheHorizons.MOD_ID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, BeyondTheHorizons.MOD_ID);

    public static final DeferredBlock<AnhydriteCrustBlock> ANHYDRITE_CRUST =
            BLOCKS.register("anhydrite_crust", () -> new AnhydriteCrustBlock(
                    BlockBehaviour.Properties.of().strength(3.0f).noCollission()
                            .noOcclusion().sound(SoundType.CALCITE)));

    public static final DeferredHolder<Item, Item> ANHYDRITE_CRUST_ITEM =
            ITEMS.register("anhydrite_crust", () -> new BlockItem(ANHYDRITE_CRUST.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> RAW_ANHYDRITE =
            ITEMS.register("raw_anhydrite", () -> new Item(new Item.Properties()));

    private VenusAnhydriteRegistry() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }
}
