package com.heroesjourney.content.batman;

import com.heroesjourney.HeroesJourney;
import com.heroesjourney.ability.Ability;
import com.heroesjourney.ability.AbilityRegistry;
import com.heroesjourney.config.HJConfig;
import com.heroesjourney.hero.HeroDefinition;
import com.heroesjourney.hero.HeroRegistry;
import com.heroesjourney.item.HJItems;
import com.heroesjourney.quest.QuestObjective;
import com.heroesjourney.quest.QuestReward;
import com.heroesjourney.quest.QuestStage;
import com.heroesjourney.quest.Questline;
import com.heroesjourney.quest.condition.EquipmentSetCondition;
import com.heroesjourney.quest.condition.JumpCountCondition;
import com.heroesjourney.quest.condition.KillCountCondition;
import com.heroesjourney.quest.condition.ProximityToEntityCondition;
import com.heroesjourney.quest.condition.PuzzleCountCondition;
import com.heroesjourney.quest.condition.RewardOnCompleteCondition;
import com.heroesjourney.quest.condition.StealthApproachCondition;
import com.heroesjourney.quest.condition.SwimDistanceCondition;
import com.heroesjourney.quest.condition.TravelDistanceCondition;
import com.heroesjourney.quest.condition.WitnessDeathCondition;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

/**
 * All the content for the "Batman - Origine" arc, declared entirely through the generic engine
 * (hero registry, quest engine, ability registry). This is the only class in the mod that knows
 * Batman-specific facts; adding the next hero later means writing a sibling of this class, not
 * touching anything under the top-level packages.
 * <p>
 * This questline uses no custom structures and no talking NPCs at all - every objective is driven
 * by vanilla mobs, vanilla stats, or items.
 */
public final class BatmanContent {

    public static final String HERO_ID = BatmanAbilities.HERO_ID;

    private BatmanContent() {
    }

    public static void register() {
        registerAbilities();
        HeroRegistry.register(new HeroDefinition(
                HERO_ID,
                Component.translatable("hero.heroesjourney.batman"),
                Component.translatable("hero.heroesjourney.batman.description"),
                ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, "textures/gui/hero_batman.png"),
                buildQuestline(),
                new BatmanEffects()
        ));
    }

    // -------------------------------------------------------------------
    // Abilities
    // -------------------------------------------------------------------

    private static void registerAbilities() {
        AbilityRegistry.register(new Ability(
                BatmanAbilities.THREAT_GLOW,
                () -> HJConfig.THREAT_GLOW_COOLDOWN_TICKS.get(),
                BatmanAbilityEffects::threatGlow
        ));
    }

    // -------------------------------------------------------------------
    // Questline
    // -------------------------------------------------------------------

    private static Questline buildQuestline() {
        List<QuestStage> stages = new ArrayList<>();

        // Quest 1 - La mort des parents.
        stages.add(new QuestStage(
                "origin_witness",
                Component.translatable("quest.heroesjourney.batman.1.title"),
                Component.translatable("quest.heroesjourney.batman.1.description"),
                List.of(new QuestObjective("witness_death", Component.translatable("objective.heroesjourney.batman.witness_death"),
                        new WitnessDeathCondition())),
                List.of(),
                List.of()
        ));

        // Quest 2 - Espionnage.
        stages.add(new QuestStage(
                "espionage",
                Component.translatable("quest.heroesjourney.batman.2.title"),
                Component.translatable("quest.heroesjourney.batman.2.description"),
                List.of(new QuestObjective("stealth_approaches", Component.translatable("objective.heroesjourney.batman.stealth_approaches"),
                        new StealthApproachCondition(HJConfig.STEALTH_APPROACH_COUNT.get(), HJConfig.STEALTH_APPROACH_RADIUS.get(),
                                HJConfig.STEALTH_APPROACH_CONSECUTIVE_SECONDS.get()))),
                List.of(reward(Component.translatable("reward.heroesjourney.batman.stealth_trained"), player ->
                        addFlag(player, BatmanAbilities.FLAG_STEALTH_TRAINED))),
                List.of()
        ));

        // Quest 3 - Physique (course / saut / nage, en parallele - chaque sous-categorie
        // recompense son propre passif des qu'elle est validee, independamment des deux autres).
        stages.add(new QuestStage(
                "physical_training",
                Component.translatable("quest.heroesjourney.batman.3.title"),
                Component.translatable("quest.heroesjourney.batman.3.description"),
                List.of(
                        new QuestObjective("run_distance", Component.translatable("objective.heroesjourney.batman.run_distance"),
                                new RewardOnCompleteCondition(new TravelDistanceCondition(HJConfig.RUN_TRAINING_DISTANCE.get()),
                                        player -> addFlag(player, BatmanAbilities.FLAG_RUN_TRAINED))),
                        new QuestObjective("jump_count", Component.translatable("objective.heroesjourney.batman.jump_count"),
                                new RewardOnCompleteCondition(new JumpCountCondition(HJConfig.JUMP_TRAINING_COUNT.get()),
                                        player -> addFlag(player, BatmanAbilities.FLAG_JUMP_TRAINED))),
                        new QuestObjective("swim_distance", Component.translatable("objective.heroesjourney.batman.swim_distance"),
                                new RewardOnCompleteCondition(new SwimDistanceCondition(HJConfig.SWIM_TRAINING_DISTANCE.get()),
                                        player -> addFlag(player, BatmanAbilities.FLAG_SWIM_TRAINED)))
                ),
                List.of(reward(Component.translatable("reward.heroesjourney.batman.riddle_book"), player ->
                        give(player, HJItems.RIDDLE_BOOK.get(), 1))),
                List.of()
        ));

        // Quest 4 - Esprit / enquete (carnet d'enigmes).
        stages.add(new QuestStage(
                "mind_investigation",
                Component.translatable("quest.heroesjourney.batman.4.title"),
                Component.translatable("quest.heroesjourney.batman.4.description"),
                List.of(new QuestObjective("solve_puzzles", Component.translatable("objective.heroesjourney.batman.solve_puzzles"),
                        new PuzzleCountCondition(HJConfig.PUZZLE_TARGET_COUNT.get()))),
                List.of(reward(Component.translatable("reward.heroesjourney.batman.threat_glow"), player ->
                        player.getData(com.heroesjourney.data.HJAttachments.HERO_DATA).getOrCreateProgress(HERO_ID)
                                .unlockAbility(BatmanAbilities.THREAT_GLOW))),
                List.of()
        ));

        // Quest 5 - Arts martiaux.
        stages.add(new QuestStage(
                "martial_arts",
                Component.translatable("quest.heroesjourney.batman.5.title"),
                Component.translatable("quest.heroesjourney.batman.5.description"),
                List.of(new QuestObjective("barehanded_kills", Component.translatable("objective.heroesjourney.batman.barehanded_kills"),
                        new KillCountCondition(HJConfig.MARTIAL_ARTS_KILLS.get(), true, false))),
                List.of(reward(Component.translatable("reward.heroesjourney.batman.martial_arts_trained"), player ->
                        addFlag(player, BatmanAbilities.FLAG_MARTIAL_ARTS_TRAINED))),
                List.of()
        ));

        // Quest 6 - La chauve-souris.
        stages.add(new QuestStage(
                "the_bat",
                Component.translatable("quest.heroesjourney.batman.6.title"),
                Component.translatable("quest.heroesjourney.batman.6.description"),
                List.of(new QuestObjective("bat_encounter", Component.translatable("objective.heroesjourney.batman.bat_encounter"),
                        new ProximityToEntityCondition(EntityType.BAT, HJConfig.BAT_PROXIMITY_RADIUS.get()))),
                List.of(reward(Component.translatable("reward.heroesjourney.batman.armor_recipes_unlocked"), player -> {
                    addFlag(player, BatmanAbilities.FLAG_ARMOR_RECIPES_UNLOCKED);
                    BatmanRecipeUnlock.grant(player, BatmanRecipeUnlock.ARMOR_ADVANCEMENT_ID);
                })),
                List.of()
        ));

        // Quest 7 - Devenir Batman.
        stages.add(new QuestStage(
                "become_batman",
                Component.translatable("quest.heroesjourney.batman.7.title"),
                Component.translatable("quest.heroesjourney.batman.7.description"),
                List.of(new QuestObjective("wear_suit", Component.translatable("objective.heroesjourney.batman.wear_suit"),
                        new EquipmentSetCondition(List.of(
                                HJItems.BAT_COWL::get, HJItems.BAT_ARMORED_CHESTPLATE::get,
                                HJItems.BAT_LEGGINGS::get, HJItems.BAT_BOOTS::get)))),
                List.of(reward(Component.translatable("reward.heroesjourney.batman.starter_kit"), player -> {
                    addFlag(player, BatmanAbilities.FLAG_GADGET_RECIPES_UNLOCKED);
                    BatmanRecipeUnlock.grant(player, BatmanRecipeUnlock.GADGETS_ADVANCEMENT_ID);
                    give(player, HJItems.GRAPPLE_HOOK.get(), 1);
                    give(player, HJItems.BATARANG.get(), 8);
                    give(player, HJItems.SMOKE_PEBBLE.get(), 3);
                })),
                List.of()
        ));

        return new Questline(HERO_ID, stages);
    }

    private static void addFlag(ServerPlayer player, String flag) {
        player.getData(com.heroesjourney.data.HJAttachments.HERO_DATA).getOrCreateProgress(HERO_ID).addFlag(flag);
    }

    private static QuestReward reward(Component summary, java.util.function.Consumer<ServerPlayer> apply) {
        return new QuestReward() {
            @Override
            public void grant(ServerPlayer player) {
                apply.accept(player);
            }

            @Override
            public Component summary() {
                return summary;
            }
        };
    }

    private static void give(ServerPlayer player, net.minecraft.world.item.Item item, int count) {
        ItemStack stack = new ItemStack(item, count);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
