package dev.ludwici.beyondthehorizons.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import dev.ludwici.beyondthehorizons.neoforge.lunar.LaserDrillItem;
import dev.ludwici.beyondthehorizons.neoforge.lunar.LunarCrystalRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3f;

import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = BeyondTheHorizons.MOD_ID, value = Dist.CLIENT)
public final class LaserDrillBeamRenderer {

    private static final DustParticleOptions IMPACT_DUST =
            new DustParticleOptions(new Vector3f(0.22f, 0.91f, 1.0f), 0.7f);
    private static final Map<Player, Integer> LAST_SPARK_TICK = new WeakHashMap<>();

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

            double time = player.tickCount + partialTick;
            double pulse = 0.5 + 0.5 * Math.sin(time * 0.52);
            renderCrossBeam(vertexConsumer, pose, start, end, 0.055 + pulse * 0.012,
                    26, 186, 244, 60 + (int) (pulse * 36));
            renderCrossBeam(vertexConsumer, pose, start, end, 0.024, 46, 227, 255, 165);
            renderCrossBeam(vertexConsumer, pose, start, end, 0.008, 220, 252, 255, 255);

            // A short bright pulse moves along the beam without allocating persistent effects.
            double travel = (time * 0.17) % 1.0;
            double tail = Math.max(0.0, travel - 0.085);
            renderCrossBeam(vertexConsumer, pose, start.lerp(end, tail), start.lerp(end, travel),
                    0.036, 151, 244, 255, 215);

            if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
                Vec3 normal = Vec3.atLowerCornerOf(blockHit.getDirection().getNormal());
                Vec3 impact = end.add(normal.scale(0.008));
                renderImpactRing(vertexConsumer, pose, impact, normal, time);
                spawnImpactSparks(minecraft, player, impact, normal, blockHit);
            }

            drawn = true;
        }

        pose.popPose();
        if (drawn) {
            bufferSource.endBatch(RenderType.lightning());
        }
    }

    private static void renderImpactRing(VertexConsumer consumer, PoseStack pose, Vec3 center,
                                         Vec3 normal, double time) {
        Vec3 u = normal.cross(new Vec3(0.0, 1.0, 0.0));
        if (u.lengthSqr() < 0.001) {
            u = normal.cross(new Vec3(1.0, 0.0, 0.0));
        }

        u = u.normalize();
        Vec3 v = normal.cross(u).normalize();
        double radius = 0.095 + 0.018 * (0.5 + 0.5 * Math.sin(time * 0.6));
        for (int segment = 0; segment < 12; segment++) {
            double a = (segment / 12.0) * Math.PI * 2.0 + time * 0.055;
            double b = ((segment + 1) / 12.0) * Math.PI * 2.0 + time * 0.055;
            Vec3 first = center.add(u.scale(Math.cos(a) * radius)).add(v.scale(Math.sin(a) * radius));
            Vec3 second = center.add(u.scale(Math.cos(b) * radius)).add(v.scale(Math.sin(b) * radius));
            Vec3 tangent = second.subtract(first);
            Vec3 width = normal.cross(tangent).normalize().scale(0.006);
            renderQuad(consumer, pose, first, second, width, 124, 236, 255, 190);
        }
    }

    private static void spawnImpactSparks(Minecraft minecraft, Player player, Vec3 impact,
                                          Vec3 normal, BlockHitResult hit) {
        int tick = player.tickCount;
        if (tick - LAST_SPARK_TICK.getOrDefault(player, -10) < 3) {
            return;
        }

        LAST_SPARK_TICK.put(player, tick);
        boolean crystal = minecraft.level.getBlockState(hit.getBlockPos())
                .is(LunarCrystalRegistry.LUNAR_CRYSTAL_BLOCK.get())
                || minecraft.level.getBlockState(hit.getBlockPos())
                .is(LunarCrystalRegistry.LUNAR_CRYSTAL_CLUSTER.get());

        double speed = 0.025;
        double x = normal.x * speed + (player.getRandom().nextDouble() - 0.5) * 0.055;
        double y = normal.y * speed + (player.getRandom().nextDouble() - 0.5) * 0.055;
        double z = normal.z * speed + (player.getRandom().nextDouble() - 0.5) * 0.055;

        minecraft.level.addParticle(IMPACT_DUST, impact.x, impact.y, impact.z, x, y, z);
        if (crystal && tick % 6 == 0) {
            minecraft.level.addParticle(ParticleTypes.END_ROD, impact.x, impact.y, impact.z, x, y, z);
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
