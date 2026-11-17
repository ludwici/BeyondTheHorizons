package dev.ludwici.beyondthehorizons.neoforge.thermal;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class ThermalGeneratorBlock extends BaseEntityBlock {

    public static final MapCodec<ThermalGeneratorBlock> CODEC = simpleCodec(
            properties -> new ThermalGeneratorBlock(properties, Mode.COOL));
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private final Mode mode;

    public ThermalGeneratorBlock(BlockBehaviour.Properties properties, Mode mode) {
        super(properties);
        this.mode = mode;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public Mode mode() {
        return mode;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ThermalGeneratorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }

        return createTickerHelper(type, ThermalGeneratorRegistry.GENERATOR_BLOCK_ENTITY.get(),
                ThermalGeneratorBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof ThermalGeneratorBlockEntity generator) {
            serverPlayer.openMenu(generator);
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public enum Mode {
        HEAT(20, 600, 80),
        COOL(-200, 70, 20);

        private final int min;
        private final int max;
        private final int initial;

        Mode(int min, int max, int initial) {
            this.min = min;
            this.max = max;
            this.initial = initial;
        }

        public int min() {
            return min;
        }

        public int max() {
            return max;
        }

        public int initial() {
            return initial;
        }
    }
}
