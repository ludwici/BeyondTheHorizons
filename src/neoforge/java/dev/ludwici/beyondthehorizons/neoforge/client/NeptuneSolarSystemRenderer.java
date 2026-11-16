package dev.ludwici.beyondthehorizons.neoforge.client;

import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import earth.terrarium.adastra.api.client.events.AdAstraClientEvents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = BeyondTheHorizons.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class NeptuneSolarSystemRenderer {

    private static final ResourceLocation SOLAR_SYSTEM = ResourceLocation.fromNamespaceAndPath("ad_astra", "solar_system");

    private NeptuneSolarSystemRenderer() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> AdAstraClientEvents.RenderSolarSystemEvent.register(NeptuneSolarSystemRenderer::render));
    }

    private static void render(GuiGraphics graphics, ResourceLocation solarSystem, int width, int height) {
        if (!SOLAR_SYSTEM.equals(solarSystem)) {
            return;
        }

        int centerX = width / 2;
        int centerY = height / 2;
        int radius = 150;

        for (int i = 0; i < 360; i++) {
            double angle = i * Math.PI / 180.0;
            int x = centerX + (int) Math.round(Math.cos(angle) * radius);
            int y = centerY + (int) Math.round(Math.sin(angle) * radius);
            graphics.fill(x, y, x + 1, y + 1, 0xff24327b);
        }

        double rotation = System.currentTimeMillis() / 8500.0;
        int planetX = centerX + (int) Math.round(Math.cos(rotation) * radius);
        int planetY = centerY + (int) Math.round(Math.sin(rotation) * radius);

        for (int y = -6; y <= 6; y++) {
            int halfWidth = (int) Math.sqrt(36 - y * y);
            graphics.fill(planetX - halfWidth, planetY + y, planetX + halfWidth + 1, planetY + y + 1,
                    y < -1 ? 0xff5dbeeb : 0xff196fb8);
        }
    }
}
