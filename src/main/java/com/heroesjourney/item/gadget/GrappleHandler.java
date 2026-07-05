package com.heroesjourney.item.gadget;

import com.heroesjourney.HeroesJourney;
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
    /** Whether the player was already sneaking at the moment the pull started - see {@link #onPlayerTick}. */
    private static final Map<UUID, Boolean> WAS_SNEAKING_AT_START = new HashMap<>();
    private static final int MAX_PULL_TICKS = 100;

    public static void startPull(ServerPlayer player, Vec3 target) {
        ACTIVE_PULLS.put(player.getUUID(), target);
        PULL_AGE.put(player.getUUID(), 0);
        WAS_SNEAKING_AT_START.put(player.getUUID(), player.isShiftKeyDown());
        HeroesJourney.LOGGER.info("[grapple-debug] {} startPull target={}", player.getGameProfile().getName(), target);
    }

    public static void cancelPull(ServerPlayer player) {
        ACTIVE_PULLS.remove(player.getUUID());
        PULL_AGE.remove(player.getUUID());
        WAS_SNEAKING_AT_START.remove(player.getUUID());
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

        // Sneaking cancels an in-progress pull, but only if the player *newly* presses sneak
        // during the pull - if they were already sneaking (e.g. carefully edging up to a ledge
        // before firing), requiring a fresh press means the pull isn't cancelled the instant it
        // starts, which is what happened before this fix: age==0's very first tick would already
        // see isShiftKeyDown()==true and cancel immediately, so the grapple looked like it did
        // nothing at all.
        boolean wasSneakingAtStart = WAS_SNEAKING_AT_START.getOrDefault(player.getUUID(), false);
        boolean sneakingNow = player.isShiftKeyDown();
        boolean sneakCancel = sneakingNow && !wasSneakingAtStart;
        if (sneakCancel || age > MAX_PULL_TICKS) {
            HeroesJourney.LOGGER.info("[grapple-debug] {} pull cancelled at age={} (sneakCancel={}, timedOut={})",
                    player.getGameProfile().getName(), age, sneakCancel, age > MAX_PULL_TICKS);
            cancelPull(player);
            return;
        }
        Vec3 toTarget = target.subtract(player.position().add(0, player.getEyeHeight() * 0.5, 0));
        double distance = toTarget.length();
        if (distance < 1.75D) {
            HeroesJourney.LOGGER.info("[grapple-debug] {} pull arrived at age={} distance={}", player.getGameProfile().getName(), age, distance);
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
        if (age % 5 == 0) {
            HeroesJourney.LOGGER.info("[grapple-debug] {} pulling age={} distance={} motion={}",
                    player.getGameProfile().getName(), age, distance, motion);
        }
    }

    @SubscribeEvent
    public void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            cancelPull(player);
        }
    }
}
