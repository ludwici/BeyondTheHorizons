
package dev.ludwici.beyondthehorizons.neoforge.mars;

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

public final class MartianHematiteRegistry {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BeyondTheHorizons.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, BeyondTheHorizons.MOD_ID);

    public static final DeferredBlock<HematiteStratumBlock> HEMATITE_STRATUM =
            BLOCKS.register("hematite_stratum", () -> new HematiteStratumBlock(
                    BlockBehaviour.Properties.of().strength(4.0f).sound(SoundType.STONE)));

    public static final DeferredHolder<Item, Item> HEMATITE_STRATUM_ITEM =
            ITEMS.register("hematite_stratum", () -> new BlockItem(HEMATITE_STRATUM.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> RAW_HEMATITE =
            ITEMS.register("raw_hematite", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> FERRITE_CORE =
            ITEMS.register("ferrite_core", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ELECTROMAGNETIC_CORE =
            ITEMS.register("electromagnetic_core", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> PRECISION_SERVO =
            ITEMS.register("precision_servo", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> STABILIZATION_MODULE =
            ITEMS.register("stabilization_module", () -> new Item(new Item.Properties()));

    private MartianHematiteRegistry() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }
}
