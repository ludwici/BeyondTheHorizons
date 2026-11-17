package dev.ludwici.beyondthehorizons.neoforge.lunar;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class LunarCrystalClusterBlock extends AmethystClusterBlock {

    public LunarCrystalClusterBlock(BlockBehaviour.Properties properties) {
        super(7.0f, 3.0f, properties);
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (player.isCreative()) {
            return super.getDestroyProgress(state, player, level, pos);
        }

        return LaserDrillItem.canMine(player.getMainHandItem()) ? LaserDrillItem.MINING_PROGRESS : 0.0f;
    }
}
