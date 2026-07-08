package com.heroesjourney.content.batman;

import com.heroesjourney.HeroesJourney;
import com.heroesjourney.config.HJConfig;
import com.heroesjourney.data.HJAttachments;
import com.heroesjourney.data.HeroData;
import com.heroesjourney.data.HeroProgress;
import com.heroesjourney.item.HJItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The passives that are naturally expressed as live event/tick checks rather than granted potion
 * effects/attribute modifiers refreshed on the slower ~1s pulse: hostile mobs occasionally failing
 * to notice the player (quest 2's "-30% detection range"), the quest-3b jump-training fall damage
 * reduction (kept separate from - and stacking with - the bat leggings' own intrinsic reduction in
 * {@code ArmorEffectsHandler}, since one is armor-intrinsic and the other is a training reward),
 * and the quest-3c swim speed bonus. The quest-5 martial-arts damage bonus is a general (not
 * unarmed-only) attribute modifier added/removed in {@code BatmanEffects} instead.
 * <p>
 * The swim speed bonus used to be Dolphin's Grace (a potion effect), which had two problems: its
 * strength wasn't finely tunable (an integer amplifier, and even amplifier 0 felt too fast), and -
 * being a potion effect with a duration - it could only be "let expire", not truly instantly
 * removed. It is a real, transient {@code water_movement_efficiency} attribute modifier instead,
 * added/removed every tick based on whether the player is *currently* in water - checked here
 * rather than in {@code BatmanEffects}'s ~1s pulse specifically so entering/leaving water reacts
 * within a single tick, not up to a second late. Like the run-training speed bonus in
 * {@code BatmanEffects}, it has two tiers - a base one, and a slightly reduced one once the full
 * 4-piece suit is also worn (the suit's weight taking a small bite out of it) - always strictly
 * above the untrained baseline of zero.
 */
public final class BatmanCombatHandler {

    private static final ResourceLocation SWIM_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, "batman_swim_training_speed");

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        AttributeInstance attribute = player.getAttribute(Attributes.WATER_MOVEMENT_EFFICIENCY);
        if (attribute == null) {
            return;
        }
        boolean shouldBoost = hasFlag(player, BatmanAbilities.FLAG_SWIM_TRAINED) && player.isInWater();
        AttributeModifier current = attribute.getModifier(SWIM_SPEED_MODIFIER_ID);
        if (shouldBoost) {
            boolean fullSuit = isWearingFullSuit(player);
            double amount = fullSuit ? HJConfig.SWIM_TRAINING_SPEED_BONUS_FULL_SUIT.get() : HJConfig.SWIM_TRAINING_SPEED_BONUS.get();
            // The full-suit tier can kick in/out while already boosted (e.g. putting the last
            // piece on while swimming) - re-add with the new amount rather than leaving a stale one.
            if (current == null || current.amount() != amount) {
                if (current != null) {
                    attribute.removeModifier(SWIM_SPEED_MODIFIER_ID);
                }
                attribute.addTransientModifier(new AttributeModifier(SWIM_SPEED_MODIFIER_ID, amount, AttributeModifier.Operation.ADD_VALUE));
            }
        } else if (current != null) {
            attribute.removeModifier(SWIM_SPEED_MODIFIER_ID);
        }
    }

    private boolean isWearingFullSuit(ServerPlayer player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(HJItems.BAT_COWL.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(HJItems.BAT_ARMORED_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(HJItems.BAT_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(HJItems.BAT_BOOTS.get());
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
