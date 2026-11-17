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
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class TemperatureDisplayBlock extends BaseEntityBlock {

    public static final MapCodec<TemperatureDisplayBlock> CODEC = simpleCodec(TemperatureDisplayBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;

    private static final VoxelShape WALL_NORTH = box(2, 3, 14, 14, 13, 16);
    private static final VoxelShape WALL_SOUTH = box(2, 3, 0, 14, 13, 2);
    private static final VoxelShape WALL_EAST = box(0, 3, 2, 2, 13, 14);
    private static final VoxelShape WALL_WEST = box(14, 3, 2, 16, 13, 14);
    private static final VoxelShape FLOOR_NORTH_SOUTH = box(2, 0, 3, 14, 2, 13);
    private static final VoxelShape FLOOR_EAST_WEST = box(3, 0, 2, 13, 2, 14);
    private static final VoxelShape CEILING_NORTH_SOUTH = box(2, 14, 3, 14, 16, 13);
    private static final VoxelShape CEILING_EAST_WEST = box(3, 14, 2, 13, 16, 14);

    public TemperatureDisplayBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FACE, AttachFace.FLOOR));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, FACE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        AttachFace face = switch (clickedFace) {
            case UP -> AttachFace.FLOOR;
            case DOWN -> AttachFace.CEILING;
            default -> AttachFace.WALL;
        };
        Direction facing = face == AttachFace.WALL
                ? clickedFace : context.getHorizontalDirection().getOpposite();
        BlockState placement = defaultBlockState().setValue(FACING, facing).setValue(FACE, face);
        return placement.canSurvive(context.getLevel(), context.getClickedPos()) ? placement : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction supportDirection = supportDirection(state);
        BlockPos supportPos = pos.relative(supportDirection);
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, supportDirection.getOpposite());
    }

    private static Direction supportDirection(BlockState state) {
        return switch (state.getValue(FACE)) {
            case FLOOR -> Direction.DOWN;
            case CEILING -> Direction.UP;
            case WALL -> state.getValue(FACING).getOpposite();
        };
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == supportDirection(state) && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACE)) {
            case FLOOR -> state.getValue(FACING).getAxis() == Direction.Axis.Z
                    ? FLOOR_NORTH_SOUTH : FLOOR_EAST_WEST;
            case CEILING -> state.getValue(FACING).getAxis() == Direction.Axis.Z
                    ? CEILING_NORTH_SOUTH : CEILING_EAST_WEST;
            case WALL -> switch (state.getValue(FACING)) {
                case NORTH -> WALL_NORTH;
                case SOUTH -> WALL_SOUTH;
                case EAST -> WALL_EAST;
                case WEST -> WALL_WEST;
                default -> WALL_NORTH;
            };
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
