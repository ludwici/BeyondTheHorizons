package dev.ludwici.beyondthehorizons.neoforge.venus;

import dev.ludwici.beyondthehorizons.neoforge.lunar.LunarCrystalRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class AnhydriteCrustBlock extends Block {

    public AnhydriteCrustBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return player.getMainHandItem().is(LunarCrystalRegistry.LASER_DRILL.get());
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (player.isCreative()) {
            return super.getDestroyProgress(state, player, level, pos);
        }

        return 0.0f;
    }
}
