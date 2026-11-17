package dev.ludwici.beyondthehorizons.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import dev.ludwici.beyondthehorizons.neoforge.lunar.LaserDrillItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = BeyondTheHorizons.MOD_ID, value = Dist.CLIENT)
public final class LaserDrillBeamRenderer {

    private LaserDrillBeamRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || event.getPoseStack() == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lightning());
        boolean drawn = false;

        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);

        for (Player player : minecraft.level.players()) {
            if (!player.isUsingItem() || player.getUsedItemHand() != InteractionHand.MAIN_HAND
                    || !LaserDrillItem.canMine(player.getUseItem())) {
                continue;
            }

            if (player.distanceToSqr(camera.x, camera.y, camera.z) > 48.0 * 48.0) {
                continue;
            }

            Vec3 direction = player.getViewVector(partialTick).normalize();
            Vec3 eye = player.getEyePosition(partialTick);
            Vec3 right = direction.cross(new Vec3(0.0, 1.0, 0.0));
            if (right.lengthSqr() < 0.001) {
                right = direction.cross(new Vec3(1.0, 0.0, 0.0));
            }

            right = right.normalize();
            Vec3 start = eye.add(direction.scale(0.24)).add(right.scale(0.17)).add(0.0, -0.15, 0.0);
            HitResult hit = player.pick(LaserDrillItem.MAX_RANGE, partialTick, false);
            Vec3 end = hit.getType() == HitResult.Type.MISS
                    ? eye.add(direction.scale(LaserDrillItem.MAX_RANGE))
                    : hit.getLocation();

            if (start.distanceToSqr(end) < 0.03) {
                continue;
            }

            renderCrossBeam(vertexConsumer, pose, start, end, 0.030, 45, 215, 255, 170);
            renderCrossBeam(vertexConsumer, pose, start, end, 0.010, 208, 249, 255, 255);
            drawn = true;
        }

        pose.popPose();
        if (drawn) {
            bufferSource.endBatch(RenderType.lightning());
        }
    }

    private static void renderCrossBeam(VertexConsumer vertexConsumer, PoseStack pose, Vec3 start, Vec3 end,
                                        double width, int red, int green, int blue, int alpha) {
        Vec3 direction = end.subtract(start).normalize();
        Vec3 across = direction.cross(new Vec3(0.0, 1.0, 0.0));
        if (across.lengthSqr() < 0.001) {
            across = direction.cross(new Vec3(1.0, 0.0, 0.0));
        }

        across = across.normalize();
        Vec3 perpendicular = direction.cross(across).normalize();
        renderQuad(vertexConsumer, pose, start, end, across.scale(width), red, green, blue, alpha);
        renderQuad(vertexConsumer, pose, start, end, perpendicular.scale(width), red, green, blue, alpha);
    }

    private static void renderQuad(VertexConsumer vertexConsumer, PoseStack pose, Vec3 start, Vec3 end,
                                   Vec3 offset, int red, int green, int blue, int alpha) {
        vertex(vertexConsumer, pose, start.add(offset), red, green, blue, alpha);
        vertex(vertexConsumer, pose, start.subtract(offset), red, green, blue, alpha);
        vertex(vertexConsumer, pose, end.subtract(offset), red, green, blue, alpha);
        vertex(vertexConsumer, pose, end.add(offset), red, green, blue, alpha);
    }

    private static void vertex(VertexConsumer vertexConsumer, PoseStack pose, Vec3 pos,
                               int red, int green, int blue, int alpha) {
        vertexConsumer.addVertex(pose.last().pose(), (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor(red, green, blue, alpha);
    }
}
