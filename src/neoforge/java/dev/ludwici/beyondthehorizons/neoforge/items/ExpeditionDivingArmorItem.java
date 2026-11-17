package dev.ludwici.beyondthehorizons.neoforge.items;

import earth.terrarium.adastra.common.items.armor.SpaceSuitItem;
import earth.terrarium.adastra.common.items.armor.base.CustomDyeableArmorItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ExpeditionDivingArmorItem extends CustomDyeableArmorItem {

    public ExpeditionDivingArmorItem(Holder<ArmorMaterial> material, ArmorItem.Type type, Item.Properties properties) {
        super(material, type, properties, 37);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.beyond_the_horizons.expedition_diving_suit").withStyle(ChatFormatting.AQUA));
    }

    public static class Chestplate extends SpaceSuitItem {

        public Chestplate(Holder<ArmorMaterial> material, Item.Properties properties) {
            super(material, ArmorItem.Type.CHESTPLATE, 1000, properties, 37);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, context, tooltip, flag);
            tooltip.add(Component.translatable("tooltip.beyond_the_horizons.expedition_diving_suit").withStyle(ChatFormatting.AQUA));
        }
    }
}
