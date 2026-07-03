package com.heroesjourney.item.armor;

import com.heroesjourney.config.HJConfig;
import com.heroesjourney.data.HJAttachments;
import com.heroesjourney.data.HeroData;
import com.heroesjourney.hero.HeroRegistry;
import com.heroesjourney.item.HJItems;
import net.minecraft.server.level.ServerPlayer;
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
 */
public final class ArmorEffectsHandler {

    private static final String BATMAN_HERO_ID = "batman_nolan";

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
            Vec3 look = player.getLookAngle();
            Vec3 motion = player.getDeltaMovement();
            double glideSpeed = HJConfig.GLIDE_HORIZONTAL_SPEED.get();
            Vec3 target = new Vec3(look.x * glideSpeed, HJConfig.GLIDE_FALL_SPEED.get(), look.z * glideSpeed);
            player.setDeltaMovement(motion.x * 0.6 + target.x * 0.4, target.y, motion.z * 0.6 + target.z * 0.4);
            player.fallDistance = 0.0F;
        }
    }

    private boolean isBatmanActive(ServerPlayer player) {
        HeroData data = player.getData(HJAttachments.HERO_DATA);
        return data.hasActiveHero() && data.activeHero().equals(BATMAN_HERO_ID);
    }
}
