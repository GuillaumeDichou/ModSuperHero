package com.heroesjourney.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Central balancing config for Hero's Journey. Every tunable value used by the generic engine
 * or by the Batman "Origine" content lives here so designers can rebalance without recompiling.
 */
public class HJConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ---------------------------------------------------------------------
    // Quest 1 - "La mort des parents"
    // ---------------------------------------------------------------------
    public static final ModConfigSpec.IntValue ORIGIN_VILLAGER_WITNESS_RADIUS = BUILDER
            .comment("Rayon (blocs) dans lequel le joueur doit se trouver pour 'assister' a la mort d'un villageois tue par un mob.")
            .defineInRange("questBatman.origin.villagerWitnessRadius", 18, 1, 128);

    // ---------------------------------------------------------------------
    // Quest 2 - "Espionnage"
    // ---------------------------------------------------------------------
    public static final ModConfigSpec.IntValue STEALTH_APPROACH_COUNT = BUILDER
            .comment("Nombre d'approches furtives distinctes requises. Reduit a 1 temporairement pour faciliter les tests.")
            .defineInRange("questBatman.stealth.approachCount", 1, 1, 1000);

    public static final ModConfigSpec.DoubleValue STEALTH_APPROACH_RADIUS = BUILDER
            .comment("Distance maximale (blocs) au mob hostile pour qu'une approche furtive compte.")
            .defineInRange("questBatman.stealth.approachRadius", 6.0, 1.0, 32.0);

    public static final ModConfigSpec.IntValue STEALTH_APPROACH_CONSECUTIVE_SECONDS = BUILDER
            .comment("Duree (secondes) pendant laquelle l'approche doit rester valide (sneak, distance, mob sans cible) avant de compter.")
            .defineInRange("questBatman.stealth.consecutiveSeconds", 3, 1, 60);

    // ---------------------------------------------------------------------
    // Quest 3 - "Physique" (3 sous-categories independantes)
    // ---------------------------------------------------------------------
    public static final ModConfigSpec.IntValue RUN_TRAINING_DISTANCE = BUILDER
            .comment("Distance (blocs, en sprint) a parcourir pour l'entrainement 'course'. Reduit a 100 temporairement pour faciliter les tests (garde un seuil > 1 pour pouvoir verifier que le comptage est proportionnel).")
            .defineInRange("questBatman.training.runDistanceBlocks", 100, 1, 1000000);

    public static final ModConfigSpec.IntValue JUMP_TRAINING_COUNT = BUILDER
            .comment("Nombre de sauts a effectuer pour l'entrainement 'saut'. Reduit a 1 temporairement pour faciliter les tests.")
            .defineInRange("questBatman.training.jumpCount", 1, 1, 1000000);

    public static final ModConfigSpec.IntValue SWIM_TRAINING_DISTANCE = BUILDER
            .comment("Distance (blocs, a la nage) a parcourir pour l'entrainement 'nage'. Reduit a 100 temporairement pour faciliter les tests (garde un seuil > 1 pour pouvoir verifier que le comptage est proportionnel).")
            .defineInRange("questBatman.training.swimDistanceBlocks", 100, 1, 1000000);

    // Both speed bonuses below have 2 tiers, always strictly above the vanilla baseline (no
    // training = no bonus at all): a base tier once trained, and a slightly reduced tier once the
    // full 4-piece suit is ALSO worn (the suit's weight taking a small bite out of the training
    // bonus, for flavor) - the full-suit tier must stay lower than the base tier but never drop to
    // (or below) the untrained baseline.
    public static final ModConfigSpec.DoubleValue SWIM_TRAINING_SPEED_BONUS = BUILDER
            .comment("Bonus de vitesse de nage (attribut water_movement_efficiency, 0.0 a 1.0) accorde par l'entrainement 'nage' sans la tenue complete, applique uniquement pendant que le joueur est dans l'eau (verifie chaque tick, pas un effet de statut).")
            .defineInRange("questBatman.training.swimSpeedBonus", 0.8, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue SWIM_TRAINING_SPEED_BONUS_FULL_SUIT = BUILDER
            .comment("Bonus de vitesse de nage quand la tenue complete (4 pieces) est en plus portee - reduit par rapport a swimSpeedBonus (poids de la tenue), mais doit rester strictement positif.")
            .defineInRange("questBatman.training.swimSpeedBonusFullSuit", 0.6, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue RUN_TRAINING_SPEED_BONUS = BUILDER
            .comment("Bonus de vitesse de deplacement (attribut movement_speed, pas un effet de statut) accorde par l'entrainement 'course' sans la tenue complete.")
            .defineInRange("questBatman.training.runSpeedBonus", 0.065, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue RUN_TRAINING_SPEED_BONUS_FULL_SUIT = BUILDER
            .comment("Bonus de vitesse de deplacement quand la tenue complete (4 pieces) est en plus portee - reduit par rapport a runSpeedBonus (poids de la tenue), mais doit rester strictement positif.")
            .defineInRange("questBatman.training.runSpeedBonusFullSuit", 0.04, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue JUMP_TRAINING_FALL_DAMAGE_REDUCTION = BUILDER
            .comment("Reduction des degats de chute (0.3 = -30%) accordee par l'entrainement 'saut'. Se cumule avec celle des jambieres.")
            .defineInRange("questBatman.training.jumpFallDamageReduction", 0.3, 0.0, 1.0);

    // ---------------------------------------------------------------------
    // Quest 4 - "Esprit / enquete" (carnet d'enigmes)
    // ---------------------------------------------------------------------
    public static final ModConfigSpec.IntValue PUZZLE_TARGET_COUNT = BUILDER
            .comment("Nombre d'enigmes a resoudre pour valider la quete. La sequence est fixe (taquin, crochetage, coffre-fort, dans cet ordre) et compte exactement 3 types - une valeur superieure repete le dernier type (coffre-fort) pour les essais suivants.")
            .defineInRange("questBatman.puzzle.targetCount", 3, 1, 1000);

    public static final ModConfigSpec.IntValue MASTERMIND_CODE_LENGTH = BUILDER
            .comment("Nombre de plots dans le code secret du mini-jeu 'coffre-fort' (Mastermind).")
            .defineInRange("questBatman.puzzle.mastermindCodeLength", 4, 2, 8);

    public static final ModConfigSpec.IntValue MASTERMIND_SYMBOL_COUNT = BUILDER
            .comment("Nombre de symboles/couleurs distincts utilisables dans le code secret (2 a 6).")
            .defineInRange("questBatman.puzzle.mastermindSymbolCount", 6, 2, 6);

    public static final ModConfigSpec.IntValue LOCKPICK_PIN_COUNT = BUILDER
            .comment("Nombre de goupilles a crocheter d'affilee pour valider le mini-jeu de crochetage.")
            .defineInRange("questBatman.puzzle.lockpickPinCount", 3, 1, 10);

    public static final ModConfigSpec.IntValue LOCKPICK_INITIAL_ZONE_WIDTH_PERCENT = BUILDER
            .comment("Largeur (pourcentage de la barre) de la zone cible pour la premiere goupille.")
            .defineInRange("questBatman.puzzle.lockpickInitialZoneWidthPercent", 30, 5, 100);

    public static final ModConfigSpec.IntValue LOCKPICK_MIN_ZONE_WIDTH_PERCENT = BUILDER
            .comment("Largeur (pourcentage de la barre) minimale de la zone cible, atteinte a la derniere goupille.")
            .defineInRange("questBatman.puzzle.lockpickMinZoneWidthPercent", 10, 2, 100);

    public static final ModConfigSpec.IntValue THREAT_GLOW_RADIUS = BUILDER
            .comment("Rayon (blocs) dans lequel la capacite 'reperage des menaces' illumine les mobs hostiles.")
            .defineInRange("questBatman.threatGlow.radius", 20, 1, 128);

    public static final ModConfigSpec.IntValue THREAT_GLOW_DURATION_TICKS = BUILDER
            .comment("Duree (ticks) de l'effet Glowing applique aux mobs hostiles. 20 ticks = 1 seconde.")
            .defineInRange("questBatman.threatGlow.durationTicks", 300, 20, 2000);

    public static final ModConfigSpec.IntValue THREAT_GLOW_COOLDOWN_TICKS = BUILDER
            .comment("Cooldown (ticks) de la capacite 'reperage des menaces'.")
            .defineInRange("questBatman.threatGlow.cooldownTicks", 600, 20, 100000);

    // ---------------------------------------------------------------------
    // Quest 5 - "Arts martiaux"
    // ---------------------------------------------------------------------
    public static final ModConfigSpec.IntValue MARTIAL_ARTS_KILLS = BUILDER
            .comment("Nombre de mobs hostiles a vaincre a mains nues (coup fatal) pour valider la quete. Reduit a 1 temporairement pour faciliter les tests.")
            .defineInRange("questBatman.martialArts.kills", 1, 1, 10000);

    public static final ModConfigSpec.DoubleValue MARTIAL_ARTS_DAMAGE_BONUS = BUILDER
            .comment("Bonus additif FIXE de degats (ADD_VALUE sur l'attribut attack_damage reel, visible par /heroesjourney stats) pour Batman actif une fois l'entrainement aux arts martiaux valide - s'ajoute tel quel au-dessus des degats de N'IMPORTE QUELLE arme (poings, epee, hache...), pas un pourcentage. Cumulatif avec FULL_SUIT_DAMAGE_BONUS.")
            .defineInRange("questBatman.martialArts.damageBonus", 1.1, 0.0, 20.0);

    // ---------------------------------------------------------------------
    // Quest 2 reward - mob detection reduction
    // ---------------------------------------------------------------------
    public static final ModConfigSpec.DoubleValue MOB_DETECTION_RANGE_MULTIPLIER = BUILDER
            .comment("Multiplicateur applique au rayon de detection des mobs hostiles envers le joueur (0.7 = -30%).")
            .defineInRange("questBatman.stealth.mobDetectionRangeMultiplier", 0.7, 0.1, 1.0);

    // ---------------------------------------------------------------------
    // Quest 6 - "La chauve-souris"
    // ---------------------------------------------------------------------
    public static final ModConfigSpec.DoubleValue BAT_PROXIMITY_RADIUS = BUILDER
            .comment("Distance (blocs) a laquelle une chauve-souris doit passer du joueur pour valider la quete.")
            .defineInRange("questBatman.bat.proximityRadius", 3.0, 1.0, 16.0);

    // ---------------------------------------------------------------------
    // Armor effects (suit-intrinsic, quest 7)
    // ---------------------------------------------------------------------
    public static final ModConfigSpec.DoubleValue FALL_DAMAGE_REDUCTION = BUILDER
            .comment("Reduction des degats de chute apportee par les jambieres elles-memes (0.3 = -30%).")
            .defineInRange("questBatman.armor.fallDamageReduction", 0.3, 0.0, 1.0);

    // ---------------------------------------------------------------------
    // Full-suit set bonus (quest 7): all 4 pieces worn + Batman active.
    // Both this and MARTIAL_ARTS_DAMAGE_BONUS are fixed ADD_VALUE bonuses (not a percentage), general
    // (any weapon, not unarmed-only), and cumulative with each other.
    // ---------------------------------------------------------------------
    public static final ModConfigSpec.DoubleValue FULL_SUIT_DAMAGE_BONUS = BUILDER
            .comment("Bonus additif FIXE de degats (ADD_VALUE sur l'attribut attack_damage) accorde quand les 4 pieces de la tenue sont portees et Batman est le heros actif, quelle que soit l'arme en main. Cumulatif avec le bonus d'entrainement aux arts martiaux, ne le remplace pas.")
            .defineInRange("questBatman.armor.fullSuitDamageBonus", 1.1, 0.0, 20.0);

    public static final ModConfigSpec.DoubleValue FULL_SUIT_ARMOR_BONUS = BUILDER
            .comment("Bonus additif de points d'armure (attribut armor) accorde quand les 4 pieces de la tenue sont portees et Batman est le heros actif.")
            .defineInRange("questBatman.armor.fullSuitArmorBonus", 2.0, 0.0, 30.0);

    public static final ModConfigSpec.DoubleValue FULL_SUIT_KNOCKBACK_RESISTANCE_BONUS = BUILDER
            .comment("Bonus additif de resistance au recul (0.2 = +20%, attribut knockback_resistance) accorde quand les 4 pieces de la tenue sont portees et Batman est le heros actif.")
            .defineInRange("questBatman.armor.fullSuitKnockbackResistanceBonus", 0.2, 0.0, 1.0);

    // ---------------------------------------------------------------------
    // Gadgets (quest 7 unlock)
    // ---------------------------------------------------------------------
    public static final ModConfigSpec.IntValue GRAPPLE_RANGE = BUILDER
            .comment("Portee maximale (blocs) du grappin.")
            .defineInRange("questBatman.gadgets.grappleRange", 24, 4, 128);

    public static final ModConfigSpec.IntValue GRAPPLE_COOLDOWN_TICKS = BUILDER
            .comment("Cooldown (ticks) du grappin.")
            .defineInRange("questBatman.gadgets.grappleCooldownTicks", 40, 0, 2000);

    public static final ModConfigSpec.DoubleValue GRAPPLE_PULL_ACCELERATION = BUILDER
            .comment("Acceleration appliquee au joueur par tick lors de la traction du grappin.")
            .defineInRange("questBatman.gadgets.grapplePullAcceleration", 0.18, 0.01, 2.0);

    public static final ModConfigSpec.DoubleValue GRAPPLE_MAX_SPEED = BUILDER
            .comment("Vitesse maximale (blocs/tick) atteinte pendant la traction du grappin.")
            .defineInRange("questBatman.gadgets.grappleMaxSpeed", 1.6, 0.1, 5.0);

    public static final ModConfigSpec.IntValue SMOKE_PEBBLE_DURATION_TICKS = BUILDER
            .comment("Duree du nuage de fumee (ticks).")
            .defineInRange("questBatman.gadgets.smokeDurationTicks", 160, 20, 1000);

    public static final ModConfigSpec.DoubleValue SMOKE_PEBBLE_RADIUS = BUILDER
            .comment("Rayon (blocs) du nuage de fumee.")
            .defineInRange("questBatman.gadgets.smokeRadius", 6.0, 1.0, 20.0);

    public static final ModConfigSpec.IntValue BATARANG_SLOW_DURATION_TICKS = BUILDER
            .comment("Duree du ralentissement inflige par un batarang qui touche sa cible (ticks).")
            .defineInRange("questBatman.gadgets.batarangSlowDurationTicks", 60, 0, 1000);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
