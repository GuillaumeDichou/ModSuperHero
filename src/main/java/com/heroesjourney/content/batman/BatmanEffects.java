package com.heroesjourney.content.batman;

import com.heroesjourney.HeroesJourney;
import com.heroesjourney.config.HJConfig;
import com.heroesjourney.data.HeroProgress;
import com.heroesjourney.hero.HeroEffectsProvider;
import com.heroesjourney.item.HJItems;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Batman's {@link HeroEffectsProvider}: grants/refreshes/removes the passive attribute-modifier
 * bonuses earned across the "Origine" questline, plus the full-suit set bonus (damage/armor/
 * knockback resistance, quest 7). Boots used to also grant their own intrinsic movement-speed
 * bonus independently of any quest flag, but that compounded multiplicatively with the
 * run-training speed bonus whenever the full suit (which includes the boots) was worn, making the
 * 4-piece set look like it included a speed bonus it was never meant to have (the full-suit bonus
 * itself never touches {@code movement_speed}, only attack damage/armor/knockback resistance) - so
 * it has been removed entirely rather than just decoupled from the full-suit check.
 * <p>
 * Every stat bonus here IS a real attribute modifier (attack damage, movement speed [run-training],
 * armor, knockback resistance) - added/removed live every pulse based on current flags/equipment,
 * so tools like {@code /heroesjourney stats} that read the vanilla attributes always show the true,
 * final value, and {@link #clear} can remove everything instantly and completely the moment Batman
 * stops being the active hero.
 * <p>
 * The martial-arts and full-suit damage bonuses are both general - they apply no matter what's in
 * the player's hand. They used to be unarmed-only (with a matching attack-speed bonus) in an
 * attempt to make bare fists competitive with a sword, but that comparison doesn't hold up against
 * vanilla's attack-cooldown system (and isn't a good fit for the setting either - a sword should
 * stay a sword). Both are now just small, general damage nudges instead.
 * <p>
 * There are no potion effects granted from this class at all anymore: night vision (cowl) and the
 * swim-training speed bonus both used to be, but a potion effect can only ever be "let expire", not
 * truly removed the instant its condition goes false without an explicit, timely
 * {@code removeEffect} call - and this class is only pulsed ~once/s, not every tick. Night vision
 * now lives in {@code ArmorEffectsHandler} (checked every tick, explicitly removed the very tick
 * the cowl comes off) and the swim speed bonus is a transient {@code water_movement_efficiency}
 * attribute modifier in {@code BatmanCombatHandler} (also checked every tick, active only while
 * actually in water) - see those classes for the reasoning in full.
 */
public final class BatmanEffects implements HeroEffectsProvider {

    private static final ResourceLocation MARTIAL_ARTS_DAMAGE_MODIFIER_ID =
            id("batman_martial_arts_damage");
    private static final ResourceLocation RUN_SPEED_MODIFIER_ID =
            id("batman_run_training_speed");
    private static final ResourceLocation FULL_SUIT_DAMAGE_MODIFIER_ID =
            id("batman_full_suit_damage");
    private static final ResourceLocation FULL_SUIT_ARMOR_MODIFIER_ID =
            id("batman_full_suit_armor");
    private static final ResourceLocation FULL_SUIT_KNOCKBACK_RESISTANCE_MODIFIER_ID =
            id("batman_full_suit_knockback_resistance");

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, path);
    }

    @Override
    public void applyImmediate(ServerPlayer player, HeroProgress progress) {
        syncModifiers(player, progress);
    }

    @Override
    public void clear(ServerPlayer player) {
        setModifier(player, Attributes.ATTACK_DAMAGE, MARTIAL_ARTS_DAMAGE_MODIFIER_ID, 0, false);
        setModifier(player, Attributes.MOVEMENT_SPEED, RUN_SPEED_MODIFIER_ID, 0, false);
        setModifier(player, Attributes.ATTACK_DAMAGE, FULL_SUIT_DAMAGE_MODIFIER_ID, 0, false);
        setModifier(player, Attributes.ARMOR, FULL_SUIT_ARMOR_MODIFIER_ID, 0, false);
        setModifier(player, Attributes.KNOCKBACK_RESISTANCE, FULL_SUIT_KNOCKBACK_RESISTANCE_MODIFIER_ID, 0, false);
    }

    @Override
    public void tick(ServerPlayer player, HeroProgress progress) {
        syncModifiers(player, progress);
    }

    /**
     * Adds/removes every attribute-modifier-based bonus this hero grants, based on the player's
     * *current* flags/equipment - called both immediately on activation and every pulse, so
     * equipping/removing a suit piece (for the run-speed/full-suit bonuses) takes effect within
     * one pulse.
     */
    private void syncModifiers(ServerPlayer player, HeroProgress progress) {
        boolean martialArtsTrained = progress.hasFlag(BatmanAbilities.FLAG_MARTIAL_ARTS_TRAINED);
        setModifier(player, Attributes.ATTACK_DAMAGE, MARTIAL_ARTS_DAMAGE_MODIFIER_ID,
                HJConfig.MARTIAL_ARTS_DAMAGE_BONUS.get(), martialArtsTrained);

        boolean fullSuit = isWearingFullSuit(player);
        boolean runTrained = progress.hasFlag(BatmanAbilities.FLAG_RUN_TRAINED);
        double runSpeedBonus = fullSuit ? HJConfig.RUN_TRAINING_SPEED_BONUS_FULL_SUIT.get() : HJConfig.RUN_TRAINING_SPEED_BONUS.get();
        setModifierMultiplied(player, Attributes.MOVEMENT_SPEED, RUN_SPEED_MODIFIER_ID, runSpeedBonus, runTrained);

        setModifier(player, Attributes.ATTACK_DAMAGE, FULL_SUIT_DAMAGE_MODIFIER_ID,
                HJConfig.FULL_SUIT_DAMAGE_BONUS.get(), fullSuit);
        setModifier(player, Attributes.ARMOR, FULL_SUIT_ARMOR_MODIFIER_ID,
                HJConfig.FULL_SUIT_ARMOR_BONUS.get(), fullSuit);
        setModifier(player, Attributes.KNOCKBACK_RESISTANCE, FULL_SUIT_KNOCKBACK_RESISTANCE_MODIFIER_ID,
                HJConfig.FULL_SUIT_KNOCKBACK_RESISTANCE_BONUS.get(), fullSuit);
    }

    private boolean isWearingFullSuit(ServerPlayer player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(HJItems.BAT_COWL.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(HJItems.BAT_ARMORED_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(HJItems.BAT_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(HJItems.BAT_BOOTS.get());
    }

    /** Adds (if missing) or removes (if present) a flat {@code ADD_VALUE} permanent modifier so the live state matches {@code shouldBeActive}. */
    private void setModifier(ServerPlayer player, Holder<Attribute> attribute, ResourceLocation id, double amount, boolean shouldBeActive) {
        setModifier(player, attribute, id, amount, AttributeModifier.Operation.ADD_VALUE, shouldBeActive);
    }

    /** Same as {@link #setModifier(ServerPlayer, Holder, ResourceLocation, double, boolean)} but as a percentage (e.g. Speed-potion-style) modifier. */
    private void setModifierMultiplied(ServerPlayer player, Holder<Attribute> attribute, ResourceLocation id, double amount, boolean shouldBeActive) {
        setModifier(player, attribute, id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, shouldBeActive);
    }

    private void setModifier(ServerPlayer player, Holder<Attribute> attribute, ResourceLocation id, double amount,
                              AttributeModifier.Operation operation, boolean shouldBeActive) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        AttributeModifier current = instance.getModifier(id);
        if (shouldBeActive) {
            // Some tiered bonuses (e.g. run-training speed) keep the same shouldBeActive but
            // change *amount* (base tier vs full-suit tier) as equipment changes - re-add with the
            // new amount rather than leaving the stale one in place if a modifier is already there.
            if (current == null || current.amount() != amount) {
                if (current != null) {
                    instance.removeModifier(id);
                }
                instance.addPermanentModifier(new AttributeModifier(id, amount, operation));
            }
        } else if (current != null) {
            instance.removeModifier(id);
        }
    }
}
