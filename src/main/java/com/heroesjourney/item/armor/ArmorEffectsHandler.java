package com.heroesjourney.item.armor;

import com.heroesjourney.config.HJConfig;
import com.heroesjourney.content.batman.BatmanAbilities;
import com.heroesjourney.data.HJAttachments;
import com.heroesjourney.data.HeroData;
import com.heroesjourney.item.HJItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Live, per-tick gating of the bat-suit's two "physical" effects (fall damage reduction, cape
 * glide). Implemented as on-demand checks rather than granted potion effects, so - unlike the
 * pulse-refreshed effects in {@code content.batman.BatmanEffects} - they turn on/off exactly
 * instantly when armor, sneak state or active hero changes; there is nothing to "remove" on
 * switch.
 * <p>
 * The glide itself is implemented by granting vanilla's own {@code Slow Falling} effect rather
 * than hand-rolling a vertical-velocity blend: the earlier hand-rolled version fought against
 * vanilla's own gravity integration (applied every tick, in the same direction, right before this
 * handler ran) and could net out to barely any perceptible slow-down. Slow Falling is a real
 * vanilla mechanic that changes the effective gravity constant itself, so it can't be "fought"
 * the same way - refreshed every tick while gliding, it just works.
 */
public final class ArmorEffectsHandler {

    @SubscribeEvent
    public void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!isBatmanActive(player)) {
            return;
        }
        if (player.getItemBySlot(EquipmentSlot.LEGS).is(HJItems.BAT_LEGGINGS.get())) {
            event.setDamageMultiplier(event.getDamageMultiplier() * (1.0F - HJConfig.FALL_DAMAGE_REDUCTION.get().floatValue()));
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!isBatmanActive(player)) {
            return;
        }
        if (!player.getItemBySlot(EquipmentSlot.CHEST).is(HJItems.BAT_ARMORED_CHESTPLATE.get())) {
            return;
        }
        boolean falling = player.getDeltaMovement().y < 0 && !player.onGround();
        if (player.isShiftKeyDown() && falling) {
            // Short duration, refreshed every tick while gliding - fades out within a second of
            // releasing sneak or landing rather than lingering, same trade-off already accepted
            // for the suit's other pulse-refreshed effects (see BatmanEffects).
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 10, 0, true, false, false));
            Vec3 look = player.getLookAngle();
            Vec3 motion = player.getDeltaMovement();
            double glideSpeed = HJConfig.GLIDE_HORIZONTAL_SPEED.get();
            player.setDeltaMovement(motion.x * 0.6 + look.x * glideSpeed * 0.4, motion.y, motion.z * 0.6 + look.z * glideSpeed * 0.4);
            player.fallDistance = 0.0F;
        }
    }

    private boolean isBatmanActive(ServerPlayer player) {
        HeroData data = player.getData(HJAttachments.HERO_DATA);
        return data.hasActiveHero() && data.activeHero().equals(BatmanAbilities.HERO_ID);
    }
}
