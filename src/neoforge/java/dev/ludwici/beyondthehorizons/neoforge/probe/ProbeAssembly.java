package dev.ludwici.beyondthehorizons.neoforge.probe;

import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class ProbeAssembly {

    public static final int MODULE_COUNT = 6;
    public static final int HULL = 0;
    public static final int PROPULSION = 1;
    public static final int POWER = 2;
    public static final int COMMUNICATION = 3;
    public static final int PROTECTION = 4;
    public static final int PAYLOAD = 5;

    public static final String PROBE_DATA_KEY = "ProbeModules";

    private ProbeAssembly() {
    }

    public static boolean accepts(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        return switch (slot) {
            case HULL -> stack.is(SpaceYRegistry.PROBE_HULL.get());
            case PROPULSION -> stack.is(SpaceYRegistry.WATER_THRUSTER.get())
                    || stack.is(SpaceYRegistry.ATMOSPHERIC_THRUSTER.get());
            case POWER -> stack.is(SpaceYRegistry.PROBE_BATTERY.get());
            case COMMUNICATION -> stack.is(SpaceYRegistry.PROBE_ANTENNA.get());
            case PROTECTION -> stack.is(SpaceYRegistry.PRESSURE_SHELL.get());
            case PAYLOAD -> stack.is(SpaceYRegistry.SAMPLE_COLLECTOR.get());
            default -> false;
        };
    }

    public static boolean canAssemble(Container input) {
        if (input.getContainerSize() < MODULE_COUNT) {
            return false;
        }

        for (int slot = 0; slot < MODULE_COUNT; slot++) {
            if (slot == PROTECTION && input.getItem(slot).isEmpty()) {
                continue;
            }

            if (!accepts(slot, input.getItem(slot))) {
                return false;
            }
        }

        return true;
    }

    public static ItemStack assemble(Container input) {
        if (!canAssemble(input)) {
            return ItemStack.EMPTY;
        }

        ItemStack probe = new ItemStack(SpaceYRegistry.MODULAR_PROBE.get());
        CompoundTag modules = new CompoundTag();
        modules.putString("hull", itemId(input.getItem(HULL)));
        modules.putString("propulsion", itemId(input.getItem(PROPULSION)));
        modules.putString("power", itemId(input.getItem(POWER)));
        modules.putString("communication", itemId(input.getItem(COMMUNICATION)));
        modules.putString("protection", itemId(input.getItem(PROTECTION)));
        modules.putString("payload", itemId(input.getItem(PAYLOAD)));
        modules.putString("environment", input.getItem(PROPULSION).is(SpaceYRegistry.WATER_THRUSTER.get())
                ? "water" : "atmosphere");
        modules.putDouble("pressureRatingBar", input.getItem(PROTECTION).isEmpty() ? 5.0 : 9.0);

        CompoundTag tag = new CompoundTag();
        tag.put(PROBE_DATA_KEY, modules);
        probe.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return probe;
    }

    public static CompoundTag getModules(ItemStack probe) {
        if (!probe.is(SpaceYRegistry.MODULAR_PROBE.get())) {
            return new CompoundTag();
        }

        CustomData data = probe.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return new CompoundTag();
        }

        return data.copyTag().getCompound(PROBE_DATA_KEY);
    }

    private static String itemId(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }

        ResourceLocation key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key == null ? BeyondTheHorizons.MOD_ID + ":unknown" : key.toString();
    }
}
