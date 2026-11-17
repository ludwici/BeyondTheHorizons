package dev.ludwici.beyondthehorizons.neoforge.venus;

import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class VenusianCompositeRegistry {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BeyondTheHorizons.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, BeyondTheHorizons.MOD_ID);

    public static final DeferredBlock<VenusianCompositeBlock> COMPOSITE =
            BLOCKS.register("venusian_composite", () -> new VenusianCompositeBlock(
                    BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.STONE)));

    public static final DeferredBlock<Block> STABILIZED_COMPOSITE =
            BLOCKS.register("stabilized_venusian_composite", () -> new Block(
                    BlockBehaviour.Properties.of().strength(2.5f).sound(SoundType.CALCITE)));

    public static final DeferredHolder<Item, Item> COMPOSITE_ITEM =
            ITEMS.register("venusian_composite", () -> new BlockItem(COMPOSITE.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> STABILIZED_COMPOSITE_ITEM =
            ITEMS.register("stabilized_venusian_composite", () -> new BlockItem(STABILIZED_COMPOSITE.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> CORUNDUM_FRAGMENT =
            ITEMS.register("corundum_fragment", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> CERAMIC_INSULATOR =
            ITEMS.register("ceramic_insulator", () -> new Item(new Item.Properties()));

    private VenusianCompositeRegistry() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }
}
