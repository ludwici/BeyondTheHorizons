package dev.ludwici.beyondthehorizons.neoforge.probe;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Locale;

public final class ModularProbeItem extends Item {

    public ModularProbeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        CompoundTag modules = ProbeAssembly.getModules(stack);

        if (!modules.contains("environment")) {
            tooltip.add(Component.translatable("tooltip.beyond_the_horizons.probe_unassembled")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltip.add(Component.translatable("tooltip.beyond_the_horizons.probe_environment",
                        Component.translatable("probe.beyond_the_horizons.environment." + modules.getString("environment")))
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.beyond_the_horizons.probe_pressure",
                        String.format(Locale.ROOT, "%.1f", modules.getDouble("pressureRatingBar")))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.beyond_the_horizons.probe_modules",
                        modules.getString("hull"),
                        modules.getString("propulsion"),
                        modules.getString("power"),
                        modules.getString("communication"),
                        modules.getString("protection").isEmpty() ? "-" : modules.getString("protection"),
                        modules.getString("payload"))
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
