package dev.ludwici.beyondthehorizons.neoforge.thermal;

import earth.terrarium.adastra.api.systems.OxygenApi;
import earth.terrarium.adastra.api.systems.TemperatureApi;
import earth.terrarium.adastra.common.config.MachineConfig;
import earth.terrarium.adastra.common.tags.ModBlockTags;
import earth.terrarium.adastra.common.utils.floodfill.FloodFill3D;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;

import java.util.HashSet;
import java.util.Set;

public final class ThermalGeneratorBlockEntity extends BlockEntity implements MenuProvider {

    public static final int CAPACITY = 50_000;
    public static final int MIN_TEMPERATURE = -200;
    public static final int MAX_TEMPERATURE = 600;
    public static final int DEFAULT_TEMPERATURE = 22;
    private static final int TEMPERATURE_INTERVAL = 20;
    private static final int DEGREES_PER_STEP = 12;

    private final InternalEnergyStorage energy = new InternalEnergyStorage();
    private Set<BlockPos> chamberPositions = Set.of();
    private int volume;
    private int targetTemperature;
    private int currentTemperature;
    private boolean temperatureInitialized;
    private int energyPerTick;
    private int status;
    private int ticks;

    public ThermalGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ThermalGeneratorRegistry.GENERATOR_BLOCK_ENTITY.get(), pos, state);
        targetTemperature = DEFAULT_TEMPERATURE;
        currentTemperature = DEFAULT_TEMPERATURE;
    }

    public EnergyStorage getEnergy() {
        return energy;
    }

    public int targetTemperature() {
        return targetTemperature;
    }

    public int currentTemperature() {
        return currentTemperature;
    }

    public int volume() {
        return volume;
    }

    public int status() {
        return status;
    }

    public int energyPerTick() {
        return energyPerTick;
    }

    public void setTargetTemperature(int temperature) {
        int sanitized = Mth.clamp(temperature, MIN_TEMPERATURE, MAX_TEMPERATURE);
        if (sanitized != targetTemperature) {
            targetTemperature = sanitized;
            setChanged();
        }
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ThermalGeneratorMenu(id, inventory, this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ThermalGeneratorBlockEntity generator) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }

        generator.tick(server, pos, state);
    }

    private void tick(ServerLevel server, BlockPos pos, BlockState state) {
        ticks++;
        if (ticks == 1 || ticks % Math.max(1, MachineConfig.distributionRefreshRate) == 0) {
            refreshChamber(server, pos, state);
        }

        if (chamberPositions.isEmpty()) {
            energyPerTick = 0;
            status = 2;
            if (temperatureInitialized && ticks % TEMPERATURE_INTERVAL == 0) {
                BlockPos outlet = pos.relative(state.getValue(ThermalGeneratorBlock.FACING));
                int ambient = baseTemperature(server, outlet);
                currentTemperature = Mth.clamp(ambient,
                        currentTemperature - DEGREES_PER_STEP, currentTemperature + DEGREES_PER_STEP);
                setChanged();
            }

            return;
        }

        BlockPos outlet = pos.relative(state.getValue(ThermalGeneratorBlock.FACING));
        int baseTemperature = baseTemperature(server, outlet);
        boolean canChange = targetTemperature != baseTemperature;
        int deviation = Math.abs(targetTemperature - baseTemperature);
        energyPerTick = canChange ? Math.max(1, 4 + volume / 45 + deviation / 45) : 0;

        boolean powered = canChange && energy.getEnergyStored() >= energyPerTick;
        if (powered) {
            energy.consume(energyPerTick);
            status = 1;
        } else {
            status = canChange ? 3 : 4;
        }

        if (ticks % TEMPERATURE_INTERVAL != 0) {
            return;
        }

        if (powered) {
            currentTemperature = Mth.clamp(targetTemperature,
                    currentTemperature - DEGREES_PER_STEP, currentTemperature + DEGREES_PER_STEP);
        } else {
            currentTemperature = Mth.clamp(baseTemperature,
                    currentTemperature - DEGREES_PER_STEP, currentTemperature + DEGREES_PER_STEP);
        }

        if (currentTemperature == baseTemperature && !powered) {
            ThermalZoneManager.clear(server, pos);
        } else {
            ThermalZoneManager.update(server, pos, chamberPositions, (short) currentTemperature);
        }

        setChanged();
    }

    private int baseTemperature(ServerLevel level, BlockPos outlet) {
        if (!OxygenApi.API.hasOxygen(level) && OxygenApi.API.hasOxygen(level, outlet)) {
            return 22;
        }

        return TemperatureApi.API.getTemperature(level);
    }

    private void refreshChamber(ServerLevel level, BlockPos pos, BlockState state) {
        BlockPos outlet = pos.relative(state.getValue(ThermalGeneratorBlock.FACING));
        Room result = scanRoom(level, outlet);
        Set<BlockPos> previous = chamberPositions;
        chamberPositions = result == null ? Set.of() : result.positions();
        volume = result == null ? 0 : result.airVolume();

        if (!previous.equals(chamberPositions)) {
            ThermalZoneManager.clear(level, pos);
            setChanged();
        }

        if (!chamberPositions.isEmpty() && !temperatureInitialized) {
            currentTemperature = baseTemperature(level, outlet);
            temperatureInitialized = true;
            setChanged();
        }
    }

    private Room scanRoom(ServerLevel level, BlockPos outlet) {
        if (!level.hasChunkAt(outlet)) {
            return null;
        }

        BlockState outletState = level.getBlockState(outlet);
        if (outletState.is(ModBlockTags.BLOCKS_FLOOD_FILL)
                || (outletState.isCollisionShapeFullBlock(level, outlet)
                && !outletState.is(ModBlockTags.PASSES_FLOOD_FILL))) {
            return null;
        }

        int limit = Math.max(2, MachineConfig.maxDistributionBlocks);
        Set<BlockPos> distributed = FloodFill3D.run(
                level, outlet, limit, FloodFill3D.TEST_FULL_SEAL, false);
        if (distributed.isEmpty() || distributed.size() >= limit) {
            return null;
        }

        Set<BlockPos> affected = new HashSet<>(distributed);
        for (BlockPos inside : distributed) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = inside.relative(direction);
                if (!level.hasChunkAt(neighbor)) {
                    return null;
                }

                if (level.getBlockState(neighbor).isCollisionShapeFullBlock(level, neighbor)) {
                    affected.add(neighbor);
                }
            }
        }

        return new Room(affected, distributed.size());
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("Energy", energy.serializeNBT(provider));
        tag.putInt("TargetTemperature", targetTemperature);
        tag.putInt("CurrentTemperature", currentTemperature);
        tag.putBoolean("TemperatureInitialized", temperatureInitialized);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("Energy")) {
            energy.deserializeNBT(provider, tag.get("Energy"));
        }

        if (tag.contains("TargetTemperature")) {
            setTargetTemperature(tag.getInt("TargetTemperature"));
        }

        if (tag.contains("CurrentTemperature")) {
            currentTemperature = tag.getInt("CurrentTemperature");
        }

        temperatureInitialized = tag.contains("TemperatureInitialized")
                ? tag.getBoolean("TemperatureInitialized") : tag.contains("CurrentTemperature");
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel server) {
            ThermalZoneManager.clear(server, worldPosition);
        }

        super.setRemoved();
    }

    private record Room(Set<BlockPos> positions, int airVolume) {
    }

    private final class InternalEnergyStorage extends EnergyStorage {

        private InternalEnergyStorage() {
            super(CAPACITY, 500, 0);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int added = super.receiveEnergy(maxReceive, simulate);
            if (!simulate && added > 0) {
                ThermalGeneratorBlockEntity.this.setChanged();
            }

            return added;
        }

        private void consume(int amount) {
            energy = Math.max(0, energy - amount);
            ThermalGeneratorBlockEntity.this.setChanged();
        }
    }
}
