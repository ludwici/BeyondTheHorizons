package dev.ludwici.beyondthehorizons.neoforge.client;

import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import dev.ludwici.beyondthehorizons.neoforge.environment.NeptuneEnvironment;
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

        float targetDistance = Mth.lerp(strength, 18.0f, 9.0f);
        event.setNearPlaneDistance(-2.0f);
        event.setFarPlaneDistance(Math.min(event.getFarPlaneDistance(), targetDistance));
        event.setCanceled(true);
    }

    private static float strength(BlockPos pos, FogType fogType) {
        if (fogType != FogType.WATER) {
            return 0.0f;
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !level.dimension().equals(NeptuneEnvironment.DIMENSION) || pos.getY() >= 40) {
            return 0.0f;
        }

        if (!level.getBiome(pos).is(PRESSURE_ABYSS)) {
            return 0.0f;
        }

        return Mth.clamp((40.0f - pos.getY()) / 14.0f, 0.45f, 1.0f);
    }
}
