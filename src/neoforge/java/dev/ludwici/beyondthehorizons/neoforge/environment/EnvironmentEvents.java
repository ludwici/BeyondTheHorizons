package dev.ludwici.beyondthehorizons.neoforge.environment;

import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class EnvironmentEvents {

    private static final ResourceKey<DamageType> PRESSURE_DAMAGE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(BeyondTheHorizons.MOD_ID, "pressure")
    );

    private static final int SAMPLE_INTERVAL = 10;
    private static final int DAMAGE_GRACE_TICKS = 60;
    private static final int DAMAGE_INTERVAL = 20;

    private static final Map<UUID, Integer> EXPOSURE_TICKS = new HashMap<>();
    private static final Set<UUID> DEBUG_PLAYERS = new HashSet<>();

    private EnvironmentEvents() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % SAMPLE_INTERVAL != 0) {
            return;
        }

        EnvironmentSnapshot environment = EnvironmentSampler.sample(player.serverLevel(), player.blockPosition());
        double resistance = PressureProtection.resistanceBar(player);
        boolean overPressure = environment.hazardousPressure() && environment.pressureBar() > resistance;
        boolean vulnerable = !player.isCreative() && !player.isSpectator();
        UUID playerId = player.getUUID();

        if (overPressure && vulnerable) {
            int exposure = EXPOSURE_TICKS.merge(playerId, SAMPLE_INTERVAL, Integer::sum);
            if (exposure >= DAMAGE_GRACE_TICKS && exposure % DAMAGE_INTERVAL == 0) {
                DamageSource source = new DamageSource(player.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(PRESSURE_DAMAGE));
                float damage = (float) Math.min(6.0, 1.0 + (environment.pressureBar() - resistance) * 0.4);
                player.hurt(source, damage);
            }
        } else {
            EXPOSURE_TICKS.remove(playerId);
        }

        if (player.tickCount % 20 != 0) {
            return;
        }

        if (DEBUG_PLAYERS.contains(playerId)) {
            player.displayClientMessage(debugMessage(environment, resistance), true);
        } else if (overPressure && vulnerable) {
            player.displayClientMessage(Component.translatable(
                    "message.beyond_the_horizons.pressure_warning",
                    format(environment.pressureBar()),
                    format(resistance)
            ).withStyle(ChatFormatting.RED), true);
        }
    }

    public static boolean toggleDebug(ServerPlayer player) {
        UUID id = player.getUUID();
        if (DEBUG_PLAYERS.remove(id)) {
            return false;
        }

        DEBUG_PLAYERS.add(id);
        return true;
    }

    public static Component inspect(ServerPlayer player) {
        EnvironmentSnapshot environment = EnvironmentSampler.sample(player.serverLevel(), player.blockPosition());
        return debugMessage(environment, PressureProtection.resistanceBar(player));
    }

    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clear(event.getEntity().getUUID());
        DEBUG_PLAYERS.remove(event.getEntity().getUUID());
    }

    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        clear(event.getEntity().getUUID());
    }

    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        clear(event.getEntity().getUUID());
    }

    private static void clear(UUID playerId) {
        EXPOSURE_TICKS.remove(playerId);
    }

    private static Component debugMessage(EnvironmentSnapshot environment, double resistance) {
        Component status = Component.translatable(environment.hazardousPressure()
                ? "message.beyond_the_horizons.environment_abyss"
                : "message.beyond_the_horizons.environment_safe");

        return Component.translatable(
                "message.beyond_the_horizons.environment_debug",
                format(environment.pressureBar()),
                format(resistance),
                environment.temperatureCelsius(),
                format(environment.wind()),
                format(environment.radiation()),
                status
        );
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
