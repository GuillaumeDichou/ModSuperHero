package com.heroesjourney.content.batman;

import com.heroesjourney.HeroesJourney;
import com.heroesjourney.config.HJConfig;
import com.heroesjourney.data.HeroProgress;
import com.heroesjourney.hero.HeroEffectsProvider;
import com.heroesjourney.item.HJItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Batman's {@link HeroEffectsProvider}: grants/refreshes/removes the passive effects earned
 * across the "Origine" questline, plus the suit's own intrinsic effects (night vision, boots
 * speed). Potion effects are refreshed roughly once a second while active and simply fade out
 * within half a minute of switching away - a deliberate simplification over frame-perfect instant
 * removal (see README). The unarmed attack-speed bonus is a real, instantly added/removed
 * attribute modifier.
 * <p>
 * Refreshed durations are kept well above vanilla's "about to expire" HUD warning threshold
 * (~200 ticks / 10s): granting e.g. 210 ticks every 20-tick refresh let the remaining duration dip
 * to ~190 ticks right before each refresh, which is inside that threshold and made the effect
 * (most visibly Night Vision's screen brightness) flicker continuously.
 */
public final class BatmanEffects implements HeroEffectsProvider {

    private static final ResourceLocation UNARMED_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, "batman_unarmed_attack_speed");

    /** Refreshed every ~1s (see {@link com.heroesjourney.effects.HeroEffectsService}); stays far above any expiry-warning threshold. */
    private static final int PULSE_EFFECT_DURATION_TICKS = 600;

    @Override
    public void applyImmediate(ServerPlayer player, HeroProgress progress) {
        if (progress.hasFlag(BatmanAbilities.FLAG_MARTIAL_ARTS_TRAINED)) {
            addAttackSpeedModifier(player);
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        removeAttackSpeedModifier(player);
    }

    @Override
    public void tick(ServerPlayer player, HeroProgress progress) {
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(HJItems.BAT_COWL.get())) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, PULSE_EFFECT_DURATION_TICKS, 0, true, false, false));
        }
        if (player.getItemBySlot(EquipmentSlot.FEET).is(HJItems.BAT_BOOTS.get())) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, PULSE_EFFECT_DURATION_TICKS, 0, true, false, false));
        }
        // Quest 3a reward: light passive sprint speed bonus.
        if (progress.hasFlag(BatmanAbilities.FLAG_RUN_TRAINED)) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, PULSE_EFFECT_DURATION_TICKS, HJConfig.RUN_TRAINING_SPEED_LEVEL.get(), true, false, false));
        }
        // Quest 3c reward: swim speed bonus.
        if (progress.hasFlag(BatmanAbilities.FLAG_SWIM_TRAINED)) {
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, PULSE_EFFECT_DURATION_TICKS, 0, true, false, false));
        }
        // Quest 5 reward: bare-handed attack speed bonus (damage bonus itself is BatmanCombatHandler).
        if (progress.hasFlag(BatmanAbilities.FLAG_MARTIAL_ARTS_TRAINED)) {
            addAttackSpeedModifier(player);
        }
    }

    private void addAttackSpeedModifier(ServerPlayer player) {
        AttributeInstance attribute = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attribute == null || attribute.getModifier(UNARMED_SPEED_MODIFIER_ID) != null) {
            return;
        }
        attribute.addPermanentModifier(new AttributeModifier(
                UNARMED_SPEED_MODIFIER_ID, HJConfig.UNARMED_ATTACK_SPEED_BONUS.get(), AttributeModifier.Operation.ADD_VALUE));
    }

    private void removeAttackSpeedModifier(ServerPlayer player) {
        AttributeInstance attribute = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attribute != null) {
            attribute.removeModifier(UNARMED_SPEED_MODIFIER_ID);
        }
    }
}
