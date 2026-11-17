package dev.ludwici.beyondthehorizons.neoforge.thermal;

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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ThermalGeneratorRegistry {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BeyondTheHorizons.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, BeyondTheHorizons.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, BeyondTheHorizons.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, BeyondTheHorizons.MOD_ID);

    public static final DeferredBlock<ThermalGeneratorBlock> HEAT_GENERATOR =
            BLOCKS.register("heat_generator", () -> new ThermalGeneratorBlock(
                    BlockBehaviour.Properties.of().strength(3.5f).sound(SoundType.METAL),
                    ThermalGeneratorBlock.Mode.HEAT));

    public static final DeferredBlock<ThermalGeneratorBlock> COLD_GENERATOR =
            BLOCKS.register("cold_generator", () -> new ThermalGeneratorBlock(
                    BlockBehaviour.Properties.of().strength(3.5f).sound(SoundType.METAL),
                    ThermalGeneratorBlock.Mode.COOL));

    public static final DeferredHolder<Item, Item> HEAT_GENERATOR_ITEM =
            ITEMS.register("heat_generator", () -> new BlockItem(HEAT_GENERATOR.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> COLD_GENERATOR_ITEM =
            ITEMS.register("cold_generator", () -> new BlockItem(COLD_GENERATOR.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ThermalGeneratorBlockEntity>> GENERATOR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("thermal_generator",
                    () -> BlockEntityType.Builder.of(ThermalGeneratorBlockEntity::new,
                            HEAT_GENERATOR.get(), COLD_GENERATOR.get()).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<ThermalGeneratorMenu>> GENERATOR_MENU =
            MENUS.register("thermal_generator", () -> new MenuType<>(ThermalGeneratorMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private ThermalGeneratorRegistry() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        modBus.addListener(ThermalGeneratorRegistry::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, GENERATOR_BLOCK_ENTITY.get(),
                (generator, direction) -> generator.getEnergy());
    }
}
