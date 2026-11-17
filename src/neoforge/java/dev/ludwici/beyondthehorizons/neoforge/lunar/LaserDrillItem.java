package dev.ludwici.beyondthehorizons.neoforge.lunar;

import dev.ludwici.beyondthehorizons.neoforge.mars.MartianHematiteRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class LaserDrillItem extends Item {

    public static final int MAX_ENERGY = 16000;
    public static final int MAX_RECEIVE = 1000;
    public static final int ENERGY_PER_CRYSTAL = 400;
    public static final double MAX_RANGE = 6.0;
    private static final int TICKS_TO_MINE = 25;
    private static final Map<ServerPlayer, MiningProgress> PROGRESS = new WeakHashMap<>();

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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || !canMine(stack) || player.isSpectator()) {
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseDuration) {
        if (!(level instanceof ServerLevel) || !(user instanceof ServerPlayer player)) {
            return;
        }

        if (!player.getMainHandItem().is(LunarCrystalRegistry.LASER_DRILL.get()) || !canMine(stack)) {
            resetProgress(player);
            player.stopUsingItem();
            return;
        }

        HitResult hit = player.pick(MAX_RANGE, 1.0f, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            resetProgress(player);
            return;
        }

        BlockPos pos = blockHit.getBlockPos();
        if (!isLaserMineable(level.getBlockState(pos))) {
            resetProgress(player);
            return;
        }

        MiningProgress progress = PROGRESS.get(player);
        if (progress == null || !progress.target.equals(pos)) {
            resetProgress(player);
            progress = new MiningProgress(pos);
            PROGRESS.put(player, progress);
        }

        progress.ticks++;
        if (progress.ticks < TICKS_TO_MINE) {
            int crackStage = Math.min(9, progress.ticks * 10 / TICKS_TO_MINE);
            level.destroyBlockProgress(breakAnimationId(player), pos, crackStage);
            return;
        }

        resetProgress(player);
        player.gameMode.destroyBlock(pos);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeCharged) {
        if (!level.isClientSide() && user instanceof ServerPlayer player) {
            resetProgress(player);
        }
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!level.isClientSide && isLaserMineable(state) && canMine(stack) && !miner.isSpectator()) {
            stack.set(LunarCrystalRegistry.LASER_DRILL_ENERGY.get(), storedEnergy(stack) - ENERGY_PER_CRYSTAL);
        }

        return true;
    }

    private static void resetProgress(ServerPlayer player) {
        MiningProgress progress = PROGRESS.remove(player);
        if (progress != null) {
            player.serverLevel().destroyBlockProgress(breakAnimationId(player), progress.target, -1);
        }
    }

    private static int breakAnimationId(ServerPlayer player) {
        return -player.getId();
    }

    private static boolean isLaserMineable(BlockState state) {
        return state.is(LunarCrystalRegistry.LUNAR_CRYSTAL_BLOCK.get())
                || state.is(LunarCrystalRegistry.LUNAR_CRYSTAL_CLUSTER.get())
                || state.is(MartianHematiteRegistry.HEMATITE_STRATUM.get());
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

    private static final class MiningProgress {

        private final BlockPos target;
        private int ticks;

        private MiningProgress(BlockPos target) {
            this.target = target.immutable();
        }
    }
}
