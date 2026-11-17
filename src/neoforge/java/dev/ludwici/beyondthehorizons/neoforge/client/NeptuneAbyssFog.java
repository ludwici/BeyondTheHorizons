package dev.ludwici.beyondthehorizons.neoforge.client;

import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import dev.ludwici.beyondthehorizons.neoforge.environment.NeptuneEnvironment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = BeyondTheHorizons.MOD_ID, value = Dist.CLIENT)
public final class NeptuneAbyssFog {

    private static final ResourceKey<Biome> PRESSURE_ABYSS = ResourceKey.create(
            Registries.BIOME, ResourceLocation.fromNamespaceAndPath(BeyondTheHorizons.MOD_ID, "pressure_abyss"));

    private static final double FOG_BLEND_DURATION_MILLIS = 1600.0;
    private static final long MAX_FRAME_DELTA_MILLIS = 250L;

    private static ClientLevel lastLevel;
    private static long lastUpdateMillis;
    private static float blendedStrength;

    private NeptuneAbyssFog() {
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        float strength = strength(event.getCamera().getBlockPosition(), event.getCamera().getFluidInCamera());
        if (strength <= 0.0f) {
            return;
        }

        event.setRed(Mth.lerp(strength, event.getRed(), 0.006f));
        event.setGreen(Mth.lerp(strength, event.getGreen(), 0.014f));
        event.setBlue(Mth.lerp(strength, event.getBlue(), 0.038f));
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        float strength = strength(event.getCamera().getBlockPosition(), event.getType());
        if (strength <= 0.0f) {
            return;
        }

        float normalNear = event.getNearPlaneDistance();
        float normalFar = event.getFarPlaneDistance();

        event.setNearPlaneDistance(Mth.lerp(strength, normalNear, -2.0f));
        event.setFarPlaneDistance(Mth.lerp(strength, normalFar, Math.min(normalFar, 9.0f)));
        event.setCanceled(true);
    }

    private static float strength(BlockPos pos, FogType fogType) {
        ClientLevel level = Minecraft.getInstance().level;
        long now = Util.getMillis();

        if (level != lastLevel) {
            lastLevel = level;
            lastUpdateMillis = now;
            blendedStrength = 0.0f;
        }

        if (level == null || fogType != FogType.WATER) {
            blendedStrength = 0.0f;
            lastUpdateMillis = now;
            return 0.0f;
        }

        float target = 0.0f;

        if (level.dimension().equals(NeptuneEnvironment.DIMENSION)
                && pos.getY() < 40
                && level.getBiome(pos).is(PRESSURE_ABYSS)) {
            target = Mth.clamp((40.0f - pos.getY()) / 16.0f, 0.0f, 1.0f);
        }

        long elapsedMillis = Math.min(MAX_FRAME_DELTA_MILLIS, Math.max(0L, now - lastUpdateMillis));
        if (elapsedMillis > 0L) {
            float blend = (float) (1.0 - Math.exp(-elapsedMillis / FOG_BLEND_DURATION_MILLIS));
            blendedStrength = Mth.lerp(blend, blendedStrength, target);
            lastUpdateMillis = now;
        }

        if (Math.abs(blendedStrength - target) < 0.001f) {
            blendedStrength = target;
        }

        return blendedStrength;
    }
}
