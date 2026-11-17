package dev.ludwici.beyondthehorizons.neoforge.probe;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.phys.BlockHitResult;

public final class SpaceYWorkbenchBlock extends BaseEntityBlock {

    public SpaceYWorkbenchBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpaceYWorkbenchBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof SpaceYWorkbenchBlockEntity workbench ? workbench : null;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof SpaceYWorkbenchBlockEntity workbench) {
            serverPlayer.openMenu(workbench);
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!oldState.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof SpaceYWorkbenchBlockEntity workbench) {
            Containers.dropContents(level, pos, workbench.components());
            level.updateNeighbourForOutputSignal(pos, this);
        }

        super.onRemove(oldState, level, pos, newState, movedByPiston);
    }
}
