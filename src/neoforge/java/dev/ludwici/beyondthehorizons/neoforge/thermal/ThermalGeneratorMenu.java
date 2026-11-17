package dev.ludwici.beyondthehorizons.neoforge.thermal;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class ThermalGeneratorMenu extends AbstractContainerMenu {

    private static final int DATA_SIZE = 8;

    private final ContainerLevelAccess access;
    private final ContainerData data;
    private final ThermalGeneratorBlockEntity generator;

    public ThermalGeneratorMenu(int id, Inventory inventory) {
        this(id, inventory, null);
    }

    public ThermalGeneratorMenu(int id, Inventory inventory, ThermalGeneratorBlockEntity generator) {
        super(ThermalGeneratorRegistry.GENERATOR_MENU.get(), id);
        this.generator = generator;
        this.access = generator == null ? ContainerLevelAccess.NULL
                : ContainerLevelAccess.create(inventory.player.level(), generator.getBlockPos());
        this.data = generator == null ? new SimpleContainerData(DATA_SIZE) : createData(generator);
        addDataSlots(this.data);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 12 + column * 18, 130 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 12 + column * 18, 188));
        }
    }

    private static ContainerData createData(ThermalGeneratorBlockEntity generator) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> generator.targetTemperature();
                    case 1 -> generator.currentTemperature();
                    case 2 -> generator.getEnergy().getEnergyStored() & 0xFFFF;
                    case 3 -> generator.getEnergy().getEnergyStored() >>> 16;
                    case 4 -> generator.energyPerTick();
                    case 5 -> generator.volume();
                    case 6 -> generator.status();
                    case 7 -> 1;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return DATA_SIZE;
            }
        };
    }

    public int targetTemperature() {
        return data.get(0);
    }

    public int currentTemperature() {
        return data.get(1);
    }

    public int storedEnergy() {
        return (data.get(2) & 0xFFFF) | ((data.get(3) & 0xFFFF) << 16);
    }

    public int energyPerTick() {
        return data.get(4);
    }

    public int volume() {
        return data.get(5);
    }

    public int status() {
        return data.get(6);
    }

    public boolean isSynchronized() {
        return data.get(7) == 1;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (generator == null || player.level().isClientSide() || !stillValid(player)
                || player.containerMenu != this || buttonId < 0 || buttonId > 800) {
            return false;
        }

        generator.setTargetTemperature(buttonId + ThermalGeneratorBlockEntity.MIN_TEMPERATURE);
        broadcastChanges();
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        if (access == ContainerLevelAccess.NULL) {
            return false;
        }

        return stillValid(access, player, generator.getBlockState().getBlock());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < 27) {
            if (!moveItemStackTo(stack, 27, 36, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, 27, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return original;
    }
}
