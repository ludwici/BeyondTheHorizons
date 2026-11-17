package dev.ludwici.beyondthehorizons.neoforge.items;

import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import earth.terrarium.adastra.common.items.armor.materials.ModArmorMaterials;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class DivingSuitItems {

    public static final double EXPEDITION_PRESSURE_BONUS = 3.0;

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, BeyondTheHorizons.MOD_ID);

    public static final DeferredHolder<Item, Item> EXPEDITION_HELMET =
            ITEMS.register("expedition_diving_helmet", () -> new ExpeditionDivingArmorItem(
                    ModArmorMaterials.SPACE_SUIT, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final DeferredHolder<Item, Item> EXPEDITION_CHESTPLATE =
            ITEMS.register("expedition_diving_chestplate", () -> new ExpeditionDivingArmorItem.Chestplate(
                    ModArmorMaterials.SPACE_SUIT, new Item.Properties()));

    public static final DeferredHolder<Item, Item> EXPEDITION_LEGGINGS =
            ITEMS.register("expedition_diving_leggings", () -> new ExpeditionDivingArmorItem(
                    ModArmorMaterials.SPACE_SUIT, ArmorItem.Type.LEGGINGS, new Item.Properties()));

    public static final DeferredHolder<Item, Item> EXPEDITION_BOOTS =
            ITEMS.register("expedition_diving_boots", () -> new ExpeditionDivingArmorItem(
                    ModArmorMaterials.SPACE_SUIT, ArmorItem.Type.BOOTS, new Item.Properties()));

    private DivingSuitItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(DivingSuitItems::onCreativeTab);
    }

    private static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(CreativeModeTabs.COMBAT)) {
            return;
        }

        event.accept(EXPEDITION_HELMET.get());
        event.accept(EXPEDITION_CHESTPLATE.get());
        event.accept(EXPEDITION_LEGGINGS.get());
        event.accept(EXPEDITION_BOOTS.get());
    }

    public static boolean isFullExpeditionSet(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).is(EXPEDITION_HELMET.get())
                && entity.getItemBySlot(EquipmentSlot.CHEST).is(EXPEDITION_CHESTPLATE.get())
                && entity.getItemBySlot(EquipmentSlot.LEGS).is(EXPEDITION_LEGGINGS.get())
                && entity.getItemBySlot(EquipmentSlot.FEET).is(EXPEDITION_BOOTS.get());
    }

    public static double protectionBonus(LivingEntity entity) {
        return isFullExpeditionSet(entity) ? EXPEDITION_PRESSURE_BONUS : 0.0;
    }
}
