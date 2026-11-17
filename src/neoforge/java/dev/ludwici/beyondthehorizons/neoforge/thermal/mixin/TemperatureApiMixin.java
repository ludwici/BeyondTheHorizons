
package dev.ludwici.beyondthehorizons.neoforge.thermal.mixin;

import dev.ludwici.beyondthehorizons.neoforge.thermal.ThermalZoneManager;
import earth.terrarium.adastra.common.systems.TemperatureApiImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TemperatureApiImpl.class, remap = false)
public abstract class TemperatureApiMixin {

    @Inject(method = "getTemperature(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)S",
            at = @At("RETURN"), cancellable = true, remap = false)
    private void bth$resolveManagedTemperature(Level level, BlockPos pos, CallbackInfoReturnable<Short> result) {
        if (level instanceof ServerLevel server) {
            Short temperature = ThermalZoneManager.getTemperature(server, pos);
            if (temperature != null) {
                result.setReturnValue(temperature);
            }
        }
    }
}
