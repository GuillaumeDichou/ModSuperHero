package com.heroesjourney.content.batman;

import com.heroesjourney.config.HJConfig;
import com.heroesjourney.data.HJAttachments;
import com.heroesjourney.data.HeroData;
import com.heroesjourney.data.HeroProgress;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

/**
 * The passives that are naturally expressed as live event checks rather than granted potion
 * effects: bonus unarmed damage (quest 5), hostile mobs occasionally failing to notice the player
 * (quest 2's "-30% detection range"), and the quest-3b jump-training fall damage reduction (kept
 * separate from - and stacking with - the bat leggings' own intrinsic reduction in
 * {@code ArmorEffectsHandler}, since one is armor-intrinsic and the other is a training reward).
 */
public final class BatmanCombatHandler {

    @SubscribeEvent
    public void onDamage(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!hasFlag(player, BatmanAbilities.FLAG_MARTIAL_ARTS_TRAINED) || !player.getMainHandItem().isEmpty()) {
            return;
        }
        event.setNewDamage(event.getNewDamage() + HJConfig.UNARMED_DAMAGE_BONUS.get().floatValue());
    }

    @SubscribeEvent
    public void onChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getNewAboutToBeSetTarget() instanceof ServerPlayer player)) {
            return;
        }
        if (!hasFlag(player, BatmanAbilities.FLAG_STEALTH_TRAINED)) {
            return;
        }
        double reduction = 1.0 - HJConfig.MOB_DETECTION_RANGE_MULTIPLIER.get();
        if (player.getRandom().nextDouble() < reduction) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!hasFlag(player, BatmanAbilities.FLAG_JUMP_TRAINED)) {
            return;
        }
        event.setDamageMultiplier(event.getDamageMultiplier() * (1.0F - HJConfig.JUMP_TRAINING_FALL_DAMAGE_REDUCTION.get().floatValue()));
    }

    private boolean hasFlag(ServerPlayer player, String flag) {
        HeroData data = player.getData(HJAttachments.HERO_DATA);
        if (!data.hasActiveHero() || !data.activeHero().equals(BatmanAbilities.HERO_ID)) {
            return false;
        }
        HeroProgress progress = data.getProgress(BatmanAbilities.HERO_ID);
        return progress != null && progress.hasFlag(flag);
    }
}
