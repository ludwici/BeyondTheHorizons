package dev.ludwici.beyondthehorizons.neoforge.probe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class SpaceYWorkbenchBlockEntity extends BlockEntity implements MenuProvider {

    private final SimpleContainer components = new SimpleContainer(ProbeAssembly.MODULE_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            SpaceYWorkbenchBlockEntity.this.setChanged();
        }
    };

    public SpaceYWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(SpaceYRegistry.WORKBENCH_BLOCK_ENTITY.get(), pos, state);
    }

    public SimpleContainer components() {
        return components;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.beyond_the_horizons.spacey_workbench");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SpaceYWorkbenchMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        NonNullList<ItemStack> stacks = NonNullList.withSize(ProbeAssembly.MODULE_COUNT, ItemStack.EMPTY);
        for (int slot = 0; slot < ProbeAssembly.MODULE_COUNT; slot++) {
            stacks.set(slot, components.getItem(slot));
        }
        ContainerHelper.saveAllItems(tag, stacks, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        NonNullList<ItemStack> stacks = NonNullList.withSize(ProbeAssembly.MODULE_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, stacks, registries);
        for (int slot = 0; slot < ProbeAssembly.MODULE_COUNT; slot++) {
            components.setItem(slot, stacks.get(slot));
        }
    }
}
