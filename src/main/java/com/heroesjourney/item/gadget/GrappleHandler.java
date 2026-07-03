package com.heroesjourney.item.gadget;

import com.heroesjourney.config.HJConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Drives the grapple's "pull" phase every tick: this is the mechanic the design brief singles
 * out as needing the most care, so it is kept in its own small, focused class rather than folded
 * into a generic handler.
 */
public final class GrappleHandler {

    private static final Map<UUID, Vec3> ACTIVE_PULLS = new HashMap<>();
    private static final Map<UUID, Integer> PULL_AGE = new HashMap<>();
    private static final int MAX_PULL_TICKS = 100;

    public static void startPull(ServerPlayer player, Vec3 target) {
        ACTIVE_PULLS.put(player.getUUID(), target);
        PULL_AGE.put(player.getUUID(), 0);
    }

    public static void cancelPull(ServerPlayer player) {
        ACTIVE_PULLS.remove(player.getUUID());
        PULL_AGE.remove(player.getUUID());
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Vec3 target = ACTIVE_PULLS.get(player.getUUID());
        if (target == null) {
            return;
        }
        int age = PULL_AGE.merge(player.getUUID(), 1, Integer::sum);
        if (player.isShiftKeyDown() || age > MAX_PULL_TICKS) {
            cancelPull(player);
            return;
        }
        Vec3 toTarget = target.subtract(player.position().add(0, player.getEyeHeight() * 0.5, 0));
        double distance = toTarget.length();
        if (distance < 1.75D) {
            cancelPull(player);
            return;
        }
        Vec3 direction = toTarget.normalize();
        double maxSpeed = HJConfig.GRAPPLE_MAX_SPEED.get();
        double acceleration = HJConfig.GRAPPLE_PULL_ACCELERATION.get();
        Vec3 motion = player.getDeltaMovement().add(direction.scale(acceleration));
        if (motion.length() > maxSpeed) {
            motion = motion.normalize().scale(maxSpeed);
        }
        player.setDeltaMovement(motion);
        player.fallDistance = 0.0F;
        player.hasImpulse = true;
    }

    @SubscribeEvent
    public void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            cancelPull(player);
        }
    }
}
