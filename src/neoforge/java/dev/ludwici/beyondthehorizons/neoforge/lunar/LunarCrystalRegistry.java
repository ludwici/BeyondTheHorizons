package dev.ludwici.beyondthehorizons.neoforge.lunar;

import com.mojang.serialization.Codec;
import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.ComponentEnergyStorage;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LunarCrystalRegistry {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BeyondTheHorizons.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, BeyondTheHorizons.MOD_ID);
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(
            BeyondTheHorizons.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> LASER_DRILL_ENERGY =
            COMPONENTS.registerComponentType("laser_drill_energy",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredBlock<LunarCrystalBlock> LUNAR_CRYSTAL_BLOCK =
            BLOCKS.register("lunar_crystal_block", () -> new LunarCrystalBlock(
                    BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.AMETHYST).lightLevel(state -> 4)));

    public static final DeferredBlock<LunarCrystalClusterBlock> LUNAR_CRYSTAL_CLUSTER =
            BLOCKS.register("lunar_crystal_cluster", () -> new LunarCrystalClusterBlock(
                    BlockBehaviour.Properties.of().strength(1.5f).noOcclusion()
                            .sound(SoundType.AMETHYST_CLUSTER).lightLevel(state -> 9)));

    public static final DeferredHolder<Item, Item> LUNAR_CRYSTAL_BLOCK_ITEM =
            ITEMS.register("lunar_crystal_block", () -> new BlockItem(LUNAR_CRYSTAL_BLOCK.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> LUNAR_CRYSTAL_CLUSTER_ITEM =
            ITEMS.register("lunar_crystal_cluster",
                    () -> new BlockItem(LUNAR_CRYSTAL_CLUSTER.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> LUNAR_CRYSTAL =
            ITEMS.register("lunar_crystal", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> POLISHED_LUNAR_CRYSTAL =
            ITEMS.register("polished_lunar_crystal", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> PRECISION_OPTICAL_LENS =
            ITEMS.register("precision_optical_lens", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, LaserDrillItem> LASER_DRILL =
            ITEMS.register("laser_drill", () -> new LaserDrillItem(
                    new Item.Properties().stacksTo(1).component(LASER_DRILL_ENERGY.get(), LaserDrillItem.MAX_ENERGY)));

    private LunarCrystalRegistry() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(LunarCrystalRegistry::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.EnergyStorage.ITEM,
                (stack, context) -> new ComponentEnergyStorage(stack, LASER_DRILL_ENERGY.get(),
                        LaserDrillItem.MAX_ENERGY, LaserDrillItem.MAX_RECEIVE, 0),
                LASER_DRILL.get());
    }
}
