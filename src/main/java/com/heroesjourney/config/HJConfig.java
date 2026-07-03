package com.heroesjourney.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Central balancing config for Hero's Journey. Every tunable value used by the generic engine
 * or by the Batman Begins content lives here so designers can rebalance without recompiling.
 * <p>
 * Structure rarity/biome placement is controlled by the datapack worldgen JSON files under
 * {@code data/heroesjourney/worldgen/structure_set/*.json} (spacing/separation) rather than this
 * TOML config, because vanilla structure placement is only reloadable through datapacks - see
 * the comment at the top of each structure_set file for the values that mirror the table in the
 * design document.
 */
public class HJConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ---------------------------------------------------------------------
    // Quest 3 - League of Shadows training thresholds
    // ---------------------------------------------------------------------
    public static final ModConfigSpec.IntValue TRAINING_BAREHANDED_KILLS = BUILDER
            .comment("Nombre de mobs hostiles a vaincre a mains nues pour l'entrainement de la Ligue des Ombres.")
            .defineInRange("questBatman.training.barehandedKills", 30, 1, 10000);

    public static final ModConfigSpec.IntValue TRAINING_RUN_DISTANCE = BUILDER
            .comment("Distance (en blocs, en sprint) a parcourir pour l'entrainement.")
            .defineInRange("questBatman.training.runDistanceBlocks", 3000, 1, 1000000);

    public static final ModConfigSpec.IntValue TRAINING_SNEAK_ATTACKS = BUILDER
            .comment("Nombre de sneak-attacks (attaque surprise sur un mob qui n'a pas detecte le joueur) requis.")
            .defineInRange("questBatman.training.sneakAttacks", 15, 1, 10000);

    // ---------------------------------------------------------------------
    // Detective sense ability (quest 2 reward)
    // ---------------------------------------------------------------------
    public static final ModConfigSpec.IntValue DETECTIVE_SENSE_RADIUS = BUILDER
            .comment("Rayon (en blocs) dans lequel le Sens du Detective revele les coffres.")
            .defineInRange("questBatman.detectiveSense.radius", 20, 1, 128);

    public static final ModConfigSpec.IntValue DETECTIVE_SENSE_DURATION_TICKS = BUILDER
            .comment("Duree (en ticks) de l'effet du Sens du Detective. 20 ticks = 1 seconde.")
            .defineInRange("questBatman.detectiveSense.durationTicks", 100, 20, 2000);

    public static final ModConfigSpec.IntValue DETECTIVE_SENSE_COOLDOWN_TICKS = BUILDER
            .comment("Cooldown (en ticks) du Sens du Detective.")
            .defineInRange("questBatman.detectiveSense.cooldownTicks", 600, 20, 100000);

    // ---------------------------------------------------------------------
    // Quest 4 passive rewards (Ken defeated)
    // ---------------------------------------------------------------------
    public static final ModConfigSpec.DoubleValue UNARMED_DAMAGE_BONUS = BUILDER
            .comment("Bonus additif aux degats a mains nues pour Batman Nolan actif.")
            .defineInRange("questBatman.passives.unarmedDamageBonus", 2.0, 0.0, 20.0);

    public static final ModConfigSpec.DoubleValue UNARMED_ATTACK_SPEED_BONUS = BUILDER
            .comment("Bonus additif a la vitesse d'attaque a mains nues.")
            .defineInRange("questBatman.passives.unarmedAttackSpeedBonus", 1.0, 0.0, 10.0);

    public static final ModConfigSpec.DoubleValue MOB_DETECTION_RANGE_MULTIPLIER = BUILDER
            .comment("Multiplicateur applique au rayon de detection des mobs hostiles envers le joueur (0.7 = -30%).")
            .defineInRange("questBatman.passives.mobDetectionRangeMultiplier", 0.7, 0.1, 1.0);

    public static final ModConfigSpec.IntValue PASSIVE_RESISTANCE_LEVEL = BUILDER
            .comment("Niveau (0 = niveau I) de Resistance permanente accordee.")
            .defineInRange("questBatman.passives.resistanceAmplifier", 0, 0, 4);

    public static final ModConfigSpec.IntValue PASSIVE_SPEED_LEVEL = BUILDER
            .comment("Niveau (0 = niveau I) de Vitesse permanente accordee.")
            .defineInRange("questBatman.passives.speedAmplifier", 0, 0, 4);

    // ---------------------------------------------------------------------
    // Armor effects
    // ---------------------------------------------------------------------
    public static final ModConfigSpec.DoubleValue FALL_DAMAGE_REDUCTION = BUILDER
            .comment("Reduction des degats de chute apportee par les jambieres (0.3 = -30%).")
            .defineInRange("questBatman.armor.fallDamageReduction", 0.3, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue GLIDE_FALL_SPEED = BUILDER
            .comment("Vitesse de chute verticale (blocs/tick, negatif = vers le bas) lorsque le plastron capature est deploye.")
            .defineInRange("questBatman.armor.glideFallSpeed", -0.09, -1.0, -0.01);

    public static final ModConfigSpec.DoubleValue GLIDE_HORIZONTAL_SPEED = BUILDER
            .comment("Vitesse horizontale (blocs/tick) appliquee en direction du regard pendant le plane.")
            .defineInRange("questBatman.armor.glideHorizontalSpeed", 0.12, 0.0, 1.0);

    // ---------------------------------------------------------------------
    // Gadgets
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

    // ---------------------------------------------------------------------
    // Boss stats
    // ---------------------------------------------------------------------
    public static final ModConfigSpec.DoubleValue KEN_HEALTH = BUILDER
            .comment("Points de vie de Ken (boss quete 4).")
            .defineInRange("questBatman.bosses.ken.health", 150.0, 1.0, 5000.0);

    public static final ModConfigSpec.DoubleValue SCARECROW_HEALTH = BUILDER
            .comment("Points de vie de Scarecrow (boss quete 6).")
            .defineInRange("questBatman.bosses.scarecrow.health", 200.0, 1.0, 5000.0);

    public static final ModConfigSpec.DoubleValue HENRI_HEALTH = BUILDER
            .comment("Points de vie de Henri / Ra's al Ghul (boss final quete 7).")
            .defineInRange("questBatman.bosses.henri.health", 250.0, 1.0, 5000.0);

    public static final ModConfigSpec.IntValue BOSS_RESPAWN_DELAY_TICKS = BUILDER
            .comment("Delai avant qu'un boss respawn a son point d'ancrage apres avoir ete vaincu. 36000 ticks = 30 minutes de jeu (a la vitesse normale).")
            .defineInRange("questBatman.bosses.respawnDelayTicks", 36000, 200, 1000000);

    public static final ModConfigSpec.IntValue BOSS_CREDIT_DAMAGE_WINDOW_TICKS = BUILDER
            .comment("Fenetre (ticks) pendant laquelle un joueur ayant inflige des degats a un boss est considere comme participant au combat pour la validation de quete.")
            .defineInRange("questBatman.bosses.creditWindowTicks", 600, 20, 12000);

    // ---------------------------------------------------------------------
    // Wayne Manor protection
    // ---------------------------------------------------------------------
    public static final ModConfigSpec.BooleanValue WAYNE_MANOR_PROTECTION_ENABLED = BUILDER
            .comment("Active la protection totale des manoirs Wayne pour les joueurs n'ayant pas termine la quete 7.")
            .define("questBatman.wayneManor.protectionEnabled", true);

    public static final ModConfigSpec.IntValue WAYNE_MANOR_PROTECTION_RADIUS = BUILDER
            .comment("Rayon (blocs, depuis le centre de la structure) considere comme faisant partie du manoir protege.")
            .defineInRange("questBatman.wayneManor.protectionRadius", 48, 4, 256);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
