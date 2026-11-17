package dev.ludwici.beyondthehorizons.neoforge.probe;

import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SpaceYRegistry {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BeyondTheHorizons.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, BeyondTheHorizons.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, BeyondTheHorizons.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, BeyondTheHorizons.MOD_ID);

    public static final DeferredBlock<SpaceYWorkbenchBlock> SPACEY_WORKBENCH =
            BLOCKS.register("spacey_workbench", () -> new SpaceYWorkbenchBlock(
                    BlockBehaviour.Properties.of().strength(3.5f).requiresCorrectToolForDrops().sound(SoundType.METAL)));

    public static final DeferredHolder<Item, Item> SPACEY_WORKBENCH_ITEM =
            ITEMS.register("spacey_workbench", () -> new BlockItem(SPACEY_WORKBENCH.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> PROBE_HULL =
            ITEMS.register("probe_hull", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> WATER_THRUSTER =
            ITEMS.register("water_thruster", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> ATMOSPHERIC_THRUSTER =
            ITEMS.register("atmospheric_thruster", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PROBE_BATTERY =
            ITEMS.register("probe_battery", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PROBE_ANTENNA =
            ITEMS.register("probe_antenna", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PRESSURE_SHELL =
            ITEMS.register("pressure_shell", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SAMPLE_COLLECTOR =
            ITEMS.register("sample_collector", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, ModularProbeItem> MODULAR_PROBE =
            ITEMS.register("modular_probe", () -> new ModularProbeItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SpaceYWorkbenchBlockEntity>> WORKBENCH_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("spacey_workbench",
                    () -> BlockEntityType.Builder.of(SpaceYWorkbenchBlockEntity::new, SPACEY_WORKBENCH.get()).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<SpaceYWorkbenchMenu>> WORKBENCH_MENU =
            MENUS.register("spacey_workbench", () -> new MenuType<>(SpaceYWorkbenchMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private SpaceYRegistry() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
    }
}
