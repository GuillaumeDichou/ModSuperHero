package com.heroesjourney.content.batman;

import com.heroesjourney.config.HJConfig;
import com.heroesjourney.data.HJAttachments;
import com.heroesjourney.data.HeroData;
import com.heroesjourney.data.HeroProgress;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Two of the quest-4 passives that are naturally expressed as live event checks rather than
 * granted effects: bonus unarmed damage, and hostile mobs occasionally failing to notice the
 * player (approximating "-30% detection range" without having to touch every hostile mob's AI).
 */
public final class BatmanCombatHandler {

    @SubscribeEvent
    public void onDamage(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!isBatmanWithPassives(player) || !player.getMainHandItem().isEmpty()) {
            return;
        }
        event.setNewDamage(event.getNewDamage() + HJConfig.UNARMED_DAMAGE_BONUS.get().floatValue());
    }

    @SubscribeEvent
    public void onChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getNewAboutToBeSetTarget() instanceof ServerPlayer player)) {
            return;
        }
        if (!isBatmanWithPassives(player)) {
            return;
        }
        double reduction = 1.0 - HJConfig.MOB_DETECTION_RANGE_MULTIPLIER.get();
        if (player.getRandom().nextDouble() < reduction) {
            event.setCanceled(true);
        }
    }

    private boolean isBatmanWithPassives(ServerPlayer player) {
        HeroData data = player.getData(HJAttachments.HERO_DATA);
        if (!data.hasActiveHero() || !data.activeHero().equals(BatmanAbilities.HERO_ID)) {
            return false;
        }
        HeroProgress progress = data.getProgress(BatmanAbilities.HERO_ID);
        return progress != null && progress.hasFlag(BatmanAbilities.FLAG_KEN_DEFEATED);
    }
}
