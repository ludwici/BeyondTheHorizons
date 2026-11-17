package dev.ludwici.beyondthehorizons.neoforge.thermal.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import dev.ludwici.beyondthehorizons.neoforge.thermal.TemperatureDisplayBlock;
import dev.ludwici.beyondthehorizons.neoforge.thermal.TemperatureDisplayBlockEntity;
import dev.ludwici.beyondthehorizons.neoforge.thermal.ThermalGeneratorRegistry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.joml.Matrix4f;
import com.mojang.math.Axis;

@EventBusSubscriber(modid = BeyondTheHorizons.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class TemperatureDisplayRenderer implements BlockEntityRenderer<TemperatureDisplayBlockEntity> {

    private final Font font;

    public TemperatureDisplayRenderer(BlockEntityRendererProvider.Context context) {
        font = context.getFont();
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ThermalGeneratorRegistry.DISPLAY_BLOCK_ENTITY.get(),
                TemperatureDisplayRenderer::new);
    }

    @Override
    public void render(TemperatureDisplayBlockEntity display, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Direction facing = display.getBlockState().getValue(TemperatureDisplayBlock.FACING);
        String value = display.hasReading() ? display.temperature() + " °C" : "-- °C";

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        pose.translate(0.0, 0.0, 0.502);
        pose.scale(0.015f, -0.027f, 0.015f);

        Matrix4f transform = pose.last().pose();
        font.drawInBatch(value, -font.width(value) / 2.0f, -font.lineHeight / 2.0f,
                0xA5F5F3, false, transform, buffers, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        pose.popPose();
    }
}
