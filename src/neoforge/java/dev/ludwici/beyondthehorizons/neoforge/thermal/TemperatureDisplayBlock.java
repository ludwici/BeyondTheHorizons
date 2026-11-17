package dev.ludwici.beyondthehorizons.neoforge.thermal;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class TemperatureDisplayBlock extends BaseEntityBlock {

    public static final MapCodec<TemperatureDisplayBlock> CODEC = simpleCodec(TemperatureDisplayBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WALL = BooleanProperty.create("wall");

    private static final VoxelShape WALL_NORTH = box(2, 3, 13, 14, 13, 15);
    private static final VoxelShape WALL_SOUTH = box(2, 3, 1, 14, 13, 3);
    private static final VoxelShape WALL_EAST = box(1, 3, 2, 3, 13, 14);
    private static final VoxelShape WALL_WEST = box(13, 3, 2, 15, 13, 14);
    private static final VoxelShape STANDING_NORTH_SOUTH = Shapes.or(
            box(2, 6, 7, 14, 14, 9), box(7, 0, 7, 9, 6, 9));
    private static final VoxelShape STANDING_EAST_WEST = Shapes.or(
            box(7, 6, 2, 9, 14, 14), box(7, 0, 7, 9, 6, 9));

    public TemperatureDisplayBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WALL, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, WALL);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        BlockState placement;
        if (clickedFace == Direction.UP) {
            placement = defaultBlockState()
                    .setValue(WALL, false)
                    .setValue(FACING, context.getHorizontalDirection().getOpposite());
        } else if (clickedFace.getAxis().isHorizontal()) {
            placement = defaultBlockState()
                    .setValue(WALL, true)
                    .setValue(FACING, clickedFace);
        } else {
            return null;
        }

        return placement.canSurvive(context.getLevel(), context.getClickedPos()) ? placement : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        boolean wall = state.getValue(WALL);
        Direction supportDirection = wall ? state.getValue(FACING).getOpposite() : Direction.DOWN;
        BlockPos supportPos = pos.relative(supportDirection);
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, supportDirection.getOpposite());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction supportDirection = state.getValue(WALL)
                ? state.getValue(FACING).getOpposite() : Direction.DOWN;
        if (direction == supportDirection && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        if (!state.getValue(WALL)) {
            return facing.getAxis() == Direction.Axis.Z ? STANDING_NORTH_SOUTH : STANDING_EAST_WEST;
        }

        return switch (facing) {
            case NORTH -> WALL_NORTH;
            case SOUTH -> WALL_SOUTH;
            case EAST -> WALL_EAST;
            case WEST -> WALL_WEST;
            default -> WALL_NORTH;
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TemperatureDisplayBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }

        return createTickerHelper(type, ThermalGeneratorRegistry.DISPLAY_BLOCK_ENTITY.get(),
                TemperatureDisplayBlockEntity::serverTick);
    }
}
