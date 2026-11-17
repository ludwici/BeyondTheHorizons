package dev.ludwici.beyondthehorizons.neoforge.probe;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class SpaceYWorkbenchMenu extends AbstractContainerMenu {

    private static final int OUTPUT_SLOT = ProbeAssembly.MODULE_COUNT;
    private static final int PLAYER_START = OUTPUT_SLOT + 1;
    private static final int PLAYER_END = PLAYER_START + 36;

    private static final int[][] MODULE_POSITIONS = {
            {14, 27}, {94, 27},
            {14, 51}, {94, 51},
            {14, 75}, {94, 75}
    };

    private final Container input;
    private final ResultContainer result = new ResultContainer();
    private final ContainerLevelAccess access;
    private final SimpleContainer tracked;
    private final net.minecraft.world.ContainerListener listener;

    public SpaceYWorkbenchMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(ProbeAssembly.MODULE_COUNT), ContainerLevelAccess.NULL);
    }

    public SpaceYWorkbenchMenu(int containerId, Inventory inventory, SpaceYWorkbenchBlockEntity workbench) {
        this(containerId, inventory, workbench.inputInventory(),
                ContainerLevelAccess.create(inventory.player.level(), workbench.getBlockPos()));
    }

    private SpaceYWorkbenchMenu(int containerId, Inventory inventory, Container input, ContainerLevelAccess access) {
        super(SpaceYRegistry.WORKBENCH_MENU.get(), containerId);
        this.input = input;
        this.access = access;
        checkContainerSize(input, ProbeAssembly.MODULE_COUNT);

        for (int slot = 0; slot < ProbeAssembly.MODULE_COUNT; slot++) {
            final int moduleSlot = slot;
            int[] position = MODULE_POSITIONS[slot];
            this.addSlot(new Slot(input, slot, position[0], position[1]) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return ProbeAssembly.accepts(moduleSlot, stack);
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }

        this.addSlot(new Slot(result, 0, 80, 105) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return ProbeAssembly.canAssemble(SpaceYWorkbenchMenu.this.input);
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                if (!player.level().isClientSide()) {
                    for (int slot = 0; slot < ProbeAssembly.MODULE_COUNT; slot++) {
                        if (!SpaceYWorkbenchMenu.this.input.getItem(slot).isEmpty()) {
                            SpaceYWorkbenchMenu.this.input.removeItem(slot, 1);
                        }
                    }

                    SpaceYWorkbenchMenu.this.slotsChanged(SpaceYWorkbenchMenu.this.input);
                }

                super.onTake(player, stack);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9,
                        8 + column * 18, 133 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column, 8 + column * 18, 191));
        }

        if (input instanceof SimpleContainer simple) {
            this.tracked = simple;
            this.listener = this::slotsChanged;
            if (access != ContainerLevelAccess.NULL) {
                simple.addListener(this.listener);
            }
        } else {
            this.tracked = null;
            this.listener = null;
        }

        this.slotsChanged(input);
    }

    @Override
    public void slotsChanged(Container container) {
        if (this.access != ContainerLevelAccess.NULL) {
            this.result.setItem(0, ProbeAssembly.assemble(this.input));
            this.broadcastChanges();
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId != 0 || player.level().isClientSide()) {
            return false;
        }

        for (int slot = 0; slot < ProbeAssembly.MODULE_COUNT; slot++) {
            if (!this.input.getItem(slot).isEmpty()) {
                return false;
            }
        }

        ItemStack[] parts = ProbeAssembly.disassemble(this.getCarried());
        if (parts.length != ProbeAssembly.MODULE_COUNT) {
            return false;
        }

        this.setCarried(ItemStack.EMPTY);
        for (int slot = 0; slot < ProbeAssembly.MODULE_COUNT; slot++) {
            this.input.setItem(slot, parts[slot]);
        }

        this.slotsChanged(this.input);
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, SpaceYRegistry.SPACEY_WORKBENCH.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (this.tracked != null && this.listener != null) {
            this.tracked.removeListener(this.listener);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index == OUTPUT_SLOT) {
            if (!this.moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }

            slot.onQuickCraft(stack, original);
            slot.onTake(player, original);
            return original;
        }

        if (index < OUTPUT_SLOT) {
            if (!this.moveItemStackTo(stack, PLAYER_START, PLAYER_END, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            boolean inserted = false;
            for (int module = 0; module < ProbeAssembly.MODULE_COUNT; module++) {
                if (ProbeAssembly.accepts(module, stack) && this.moveItemStackTo(stack, module, module + 1, false)) {
                    inserted = true;
                    break;
                }
            }

            if (!inserted) {
                if (index < PLAYER_START + 27) {
                    if (!this.moveItemStackTo(stack, PLAYER_START + 27, PLAYER_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stack, PLAYER_START, PLAYER_START + 27, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        slot.onTake(player, stack);
        return original;
    }
}
