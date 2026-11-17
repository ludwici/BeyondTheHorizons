package dev.ludwici.beyondthehorizons.neoforge.lunar;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class LaserDrillItem extends Item {

    public static final int MAX_ENERGY = 16000;
    public static final int MAX_RECEIVE = 1000;
    public static final int ENERGY_PER_CRYSTAL = 400;
    public static final float MINING_PROGRESS = 1.0f / 35.0f;

    public LaserDrillItem(Properties properties) {
        super(properties);
    }

    public static int storedEnergy(ItemStack stack) {
        return stack.getOrDefault(LunarCrystalRegistry.LASER_DRILL_ENERGY.get(), 0);
    }

    public static boolean canMine(ItemStack stack) {
        return stack.is(LunarCrystalRegistry.LASER_DRILL.get()) && storedEnergy(stack) >= ENERGY_PER_CRYSTAL;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!level.isClientSide && isLunarCrystal(state) && canMine(stack) && !miner.isSpectator()) {
            stack.set(LunarCrystalRegistry.LASER_DRILL_ENERGY.get(), storedEnergy(stack) - ENERGY_PER_CRYSTAL);
        }

        return true;
    }

    private static boolean isLunarCrystal(BlockState state) {
        return state.is(LunarCrystalRegistry.LUNAR_CRYSTAL_BLOCK.get())
                || state.is(LunarCrystalRegistry.LUNAR_CRYSTAL_CLUSTER.get());
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * storedEnergy(stack) / MAX_ENERGY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x50CFE8;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.beyond_the_horizons.laser_drill_energy",
                storedEnergy(stack), MAX_ENERGY).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.beyond_the_horizons.laser_drill_function")
                .withStyle(ChatFormatting.GRAY));
    }
}
