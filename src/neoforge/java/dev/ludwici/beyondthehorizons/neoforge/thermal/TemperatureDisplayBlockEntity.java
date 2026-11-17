package dev.ludwici.beyondthehorizons.neoforge.thermal;

import earth.terrarium.adastra.api.systems.TemperatureApi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class TemperatureDisplayBlockEntity extends BlockEntity {

    private int temperature;
    private boolean hasReading;

    public TemperatureDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(ThermalGeneratorRegistry.DISPLAY_BLOCK_ENTITY.get(), pos, state);
    }

    public int temperature() {
        return temperature;
    }

    public boolean hasReading() {
        return hasReading;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TemperatureDisplayBlockEntity display) {
        if (!(level instanceof ServerLevel server) || server.getGameTime() % 20 != 0) {
            return;
        }

        int reading = TemperatureApi.API.getTemperature(server, pos);
        if (display.hasReading && display.temperature == reading) {
            return;
        }

        display.temperature = reading;
        display.hasReading = true;
        display.setChanged();
        server.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("Temperature", temperature);
        tag.putBoolean("HasReading", hasReading);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        temperature = tag.getInt("Temperature");
        hasReading = tag.getBoolean("HasReading");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Temperature", temperature);
        tag.putBoolean("HasReading", hasReading);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
