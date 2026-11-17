package dev.ludwici.beyondthehorizons.neoforge.venus;

import dev.ludwici.beyondthehorizons.neoforge.thermal.ThermalZoneManager;
import earth.terrarium.adastra.api.systems.TemperatureApi;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

public final class VenusianCompositeBlock extends Block {

    public static final IntegerProperty COOLING = IntegerProperty.create("cooling", 0, 11);
    public static final int CHECK_INTERVAL = 100;
    public static final int MAX_COOLING_TEMPERATURE = 0;
    public static final int MIN_COOLING_TEMPERATURE = -200;

    public VenusianCompositeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(COOLING, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COOLING);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel server) {
            server.scheduleTick(pos, this, CHECK_INTERVAL);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.getBlockState(pos).is(this)) {
            return;
        }

        Short managedTemperature = ThermalZoneManager.getTemperature(level, pos);
        if (managedTemperature != null) {
            int temperature = TemperatureApi.API.getTemperature(level, pos);
            if (temperature >= MIN_COOLING_TEMPERATURE && temperature <= MAX_COOLING_TEMPERATURE) {
                int cooling = state.getValue(COOLING);
                if (cooling == 11) {
                    level.setBlock(pos, VenusianCompositeRegistry.STABILIZED_COMPOSITE.get().defaultBlockState(), Block.UPDATE_ALL);
                    return;
                }

                level.setBlock(pos, state.setValue(COOLING, cooling + 1), Block.UPDATE_CLIENTS);
            }
        }

        level.scheduleTick(pos, this, CHECK_INTERVAL);
    }
}
