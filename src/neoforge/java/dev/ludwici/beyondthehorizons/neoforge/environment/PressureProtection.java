package dev.ludwici.beyondthehorizons.neoforge.environment;

import dev.ludwici.beyondthehorizons.neoforge.items.DivingSuitItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class PressureProtection {

    public interface SealedVehicle {
        double pressureResistanceBar();
    }

    private PressureProtection() {
    }

    public static double resistanceBar(Entity entity) {
        double resistance = 0.0;

        if (entity instanceof LivingEntity living && living.getAttribute(EnvironmentAttributes.PRESSURE_RESISTANCE) != null) {
            resistance = living.getAttributeValue(EnvironmentAttributes.PRESSURE_RESISTANCE);
        }

        if (entity instanceof LivingEntity living) {
            resistance += DivingSuitItems.protectionBonus(living);
        }

        Entity vehicle = entity.getVehicle();
        if (vehicle instanceof SealedVehicle sealedVehicle) {
            resistance = Math.max(resistance, sealedVehicle.pressureResistanceBar());
        }

        return Math.max(0.0, resistance);
    }
}
