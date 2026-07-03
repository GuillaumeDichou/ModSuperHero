package com.heroesjourney.content.batman;

import com.heroesjourney.HeroesJourney;
import com.heroesjourney.ability.Ability;
import com.heroesjourney.ability.AbilityRegistry;
import com.heroesjourney.config.HJConfig;
import com.heroesjourney.dialogue.DialogueChoiceDef;
import com.heroesjourney.dialogue.DialogueNode;
import com.heroesjourney.dialogue.DialogueRegistry;
import com.heroesjourney.dialogue.DialogueTree;
import com.heroesjourney.dialogue.NpcDefinition;
import com.heroesjourney.entity.HJEntities;
import com.heroesjourney.entity.boss.BossRegistry;
import com.heroesjourney.hero.HeroDefinition;
import com.heroesjourney.hero.HeroRegistry;
import com.heroesjourney.item.HJItems;
import com.heroesjourney.item.TreasureMapFactory;
import com.heroesjourney.quest.QuestObjective;
import com.heroesjourney.quest.QuestReward;
import com.heroesjourney.quest.QuestStage;
import com.heroesjourney.quest.Questline;
import com.heroesjourney.quest.StageHook;
import com.heroesjourney.quest.condition.BossKillCondition;
import com.heroesjourney.quest.condition.EquipmentSetCondition;
import com.heroesjourney.quest.condition.KillCountCondition;
import com.heroesjourney.quest.condition.PrisonBreakCondition;
import com.heroesjourney.quest.condition.ProximityToBlockCondition;
import com.heroesjourney.quest.condition.TravelDistanceCondition;
import com.heroesjourney.structure.BuildingFeature;
import com.heroesjourney.structure.BuildingLayout;
import com.heroesjourney.structure.BuildingRegistry;
import com.heroesjourney.structure.HJStructures;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/**
 * All the content for the "Batman Begins" arc, declared entirely through the generic engine
 * (hero registry, quest engine, dialogue registry, structure/boss registries). This is the only
 * class in the mod that knows Batman-specific facts; adding the next hero later means writing a
 * sibling of this class, not touching anything under the top-level packages.
 */
public final class BatmanContent {

    public static final String HERO_ID = BatmanAbilities.HERO_ID;

    private BatmanContent() {
    }

    public static void register() {
        registerBuildings();
        registerBosses();
        registerDialogue();
        registerAbilities();
        HeroRegistry.register(new HeroDefinition(
                HERO_ID,
                Component.translatable("hero.heroesjourney.batman_nolan"),
                Component.translatable("hero.heroesjourney.batman_nolan.description"),
                ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, "textures/gui/hero_batman_nolan.png"),
                buildQuestline(),
                new BatmanEffects()
        ));
    }

    // -------------------------------------------------------------------
    // Structures
    // -------------------------------------------------------------------

    private static void registerBuildings() {
        // Wayne Manor is NOT registered here: it has its own dedicated structure/piece
        // (com.heroesjourney.structure.wayne.*) instead of using the generic BuildingLayout
        // system, since it needs a full multi-storey interior the generic "simple box" system
        // was never meant to express. See WayneManorPiece for the actual estate layout.

        BuildingRegistry.register("prison", new BuildingLayout(
                15, 5, 13,
                Blocks.SMOOTH_SANDSTONE, Blocks.SANDSTONE, Blocks.SANDSTONE_SLAB, Blocks.CUT_SANDSTONE,
                false,
                List.of(
                        BuildingFeature.of(2, 0, 3, BuildingFeature.Kind.NPC_SPAWN, "prisoner_a"),
                        BuildingFeature.of(11, 0, 3, BuildingFeature.Kind.NPC_SPAWN, "prisoner_b"),
                        BuildingFeature.of(6, 0, 9, BuildingFeature.Kind.HOSTILE_SPAWN, "guard"),
                        BuildingFeature.of(8, 0, 9, BuildingFeature.Kind.HOSTILE_SPAWN, "guard"),
                        BuildingFeature.of(2, 0, 10, BuildingFeature.Kind.CHEST, ""),
                        BuildingFeature.of(12, 0, 10, BuildingFeature.Kind.CHEST, "heroesjourney:rusty_cell_key"),
                        BuildingFeature.of(7, 1, 1, BuildingFeature.Kind.TORCH)
                )
        ));

        BuildingRegistry.register("monastery", new BuildingLayout(
                15, 6, 15,
                Blocks.POLISHED_BASALT, Blocks.SMOOTH_STONE, Blocks.SMOOTH_STONE_SLAB, Blocks.CHISELED_POLISHED_BLACKSTONE,
                true,
                List.of(
                        BuildingFeature.of(5, 0, 7, BuildingFeature.Kind.NPC_SPAWN, "ken"),
                        BuildingFeature.of(9, 0, 7, BuildingFeature.Kind.NPC_SPAWN, "henri"),
                        BuildingFeature.of(3, 0, 12, BuildingFeature.Kind.HOSTILE_SPAWN, "ninja"),
                        BuildingFeature.of(11, 0, 12, BuildingFeature.Kind.HOSTILE_SPAWN, "ninja"),
                        BuildingFeature.of(7, 0, 3, BuildingFeature.Kind.BOSS_SPAWNER, "ken"),
                        BuildingFeature.of(1, 1, 1, BuildingFeature.Kind.TORCH),
                        BuildingFeature.of(13, 1, 1, BuildingFeature.Kind.TORCH)
                )
        ));

        BuildingRegistry.register("asylum", new BuildingLayout(
                17, 7, 15,
                Blocks.DEEPSLATE_BRICKS, Blocks.DEEPSLATE_TILES, Blocks.COBBLED_DEEPSLATE, Blocks.CRACKED_DEEPSLATE_BRICKS,
                false,
                List.of(
                        BuildingFeature.of(8, 0, 12, BuildingFeature.Kind.BOSS_SPAWNER, "scarecrow"),
                        BuildingFeature.of(3, 0, 3, BuildingFeature.Kind.HOSTILE_SPAWN, "guard"),
                        BuildingFeature.of(13, 0, 3, BuildingFeature.Kind.HOSTILE_SPAWN, "guard"),
                        BuildingFeature.of(1, 1, 1, BuildingFeature.Kind.TORCH),
                        BuildingFeature.of(15, 1, 1, BuildingFeature.Kind.TORCH)
                )
        ));

        BuildingRegistry.register("train", new BuildingLayout(
                7, 4, 22,
                Blocks.IRON_BLOCK, Blocks.GRAY_CONCRETE, Blocks.LIGHT_GRAY_CONCRETE, Blocks.BLACK_CONCRETE,
                true,
                List.of(
                        BuildingFeature.of(3, 0, 11, BuildingFeature.Kind.BOSS_SPAWNER, "henri"),
                        BuildingFeature.of(1, 1, 2, BuildingFeature.Kind.TORCH),
                        BuildingFeature.of(5, 1, 19, BuildingFeature.Kind.TORCH)
                )
        ));
    }

    // -------------------------------------------------------------------
    // Bosses
    // -------------------------------------------------------------------

    private static void registerBosses() {
        BossRegistry.register("ken", () -> HJEntities.KEN_BOSS.get());
        BossRegistry.register("scarecrow", () -> HJEntities.SCARECROW_BOSS.get());
        BossRegistry.register("henri", () -> HJEntities.HENRI_BOSS.get());
    }

    // -------------------------------------------------------------------
    // Abilities
    // -------------------------------------------------------------------

    private static void registerAbilities() {
        AbilityRegistry.register(new Ability(
                BatmanAbilities.DETECTIVE_SENSE,
                () -> HJConfig.DETECTIVE_SENSE_COOLDOWN_TICKS.get(),
                BatmanAbilityEffects::detectiveSense
        ));
    }

    // -------------------------------------------------------------------
    // NPCs & dialogue
    // -------------------------------------------------------------------

    private static void registerDialogue() {
        ResourceLocation fallback = ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, "textures/entity/npc_fallback.png");

        DialogueRegistry.registerNpc(new NpcDefinition("prisoner_a", Component.translatable("npc.heroesjourney.prisoner_a"),
                fallback, HERO_ID, "prisoner_a", Component.translatable("npc.heroesjourney.neutral")));
        DialogueRegistry.registerNpc(new NpcDefinition("prisoner_b", Component.translatable("npc.heroesjourney.prisoner_b"),
                fallback, HERO_ID, "prisoner_b", Component.translatable("npc.heroesjourney.neutral")));
        DialogueRegistry.registerNpc(new NpcDefinition("ken", Component.translatable("npc.heroesjourney.ken"),
                fallback, HERO_ID, "ken", Component.translatable("npc.heroesjourney.neutral")));
        DialogueRegistry.registerNpc(new NpcDefinition("henri", Component.translatable("npc.heroesjourney.henri"),
                fallback, HERO_ID, "henri", Component.translatable("npc.heroesjourney.neutral")));

        DialogueRegistry.registerTree(DialogueTree.simple("prisoner_a", "start", Map.of(
                "start", new DialogueNode("start", Component.translatable("dialogue.heroesjourney.prisoner_a.start"),
                        List.of(DialogueChoiceDef.of("end", Component.translatable("gui.heroesjourney.dialogue.thanks"), null)))
        )));

        DialogueRegistry.registerTree(DialogueTree.simple("prisoner_b", "start", Map.of(
                "start", new DialogueNode("start", Component.translatable("dialogue.heroesjourney.prisoner_b.start"),
                        List.of(DialogueChoiceDef.of("end", Component.translatable("gui.heroesjourney.dialogue.thanks"), null)))
        )));

        DialogueRegistry.registerTree(DialogueTree.simple("henri", "start", Map.of(
                "start", new DialogueNode("start", Component.translatable("dialogue.heroesjourney.henri.start"),
                        List.of(DialogueChoiceDef.of("end", Component.translatable("gui.heroesjourney.dialogue.thanks"), null)))
        )));

        registerKenDialogue();
    }

    private static void registerKenDialogue() {
        Map<String, DialogueNode> nodes = Map.of(
                "too_early", new DialogueNode("too_early", Component.translatable("dialogue.heroesjourney.ken.too_early"),
                        List.of(DialogueChoiceDef.of("end", Component.translatable("gui.heroesjourney.dialogue.leave"), null))),
                "training_brief", new DialogueNode("training_brief", Component.translatable("dialogue.heroesjourney.ken.training_brief"),
                        List.of(DialogueChoiceDef.of("understood", Component.translatable("gui.heroesjourney.dialogue.understood"), null))),
                "order", new DialogueNode("order", Component.translatable("dialogue.heroesjourney.ken.order"),
                        List.of(
                                new DialogueChoiceDef("hesitate", Component.translatable("gui.heroesjourney.dialogue.hesitate"), "order", DialogueChoiceDef.DialogueEffect.NONE),
                                new DialogueChoiceDef("refuse", Component.translatable("gui.heroesjourney.dialogue.refuse"), null, BatmanContent::triggerKenBossFight)
                        )),
                "after", new DialogueNode("after", Component.translatable("dialogue.heroesjourney.ken.after"),
                        List.of(DialogueChoiceDef.of("end", Component.translatable("gui.heroesjourney.dialogue.leave"), null)))
        );
        DialogueTree tree = new DialogueTree("ken", "too_early", nodes, progress -> {
            int stage = progress.stageIndex();
            if (stage < 2) {
                return "too_early";
            } else if (stage == 2) {
                return "training_brief";
            } else if (stage == 3) {
                return "order";
            }
            return "after";
        });
        DialogueRegistry.registerTree(tree);
    }

    private static void triggerKenBossFight(ServerPlayer player, ServerLevel level, com.heroesjourney.entity.npc.QuestNpcEntity npc) {
        net.minecraft.core.BlockPos pos = npc.blockPosition();
        float yaw = npc.getYRot();
        npc.discard();
        Entity boss = HJEntities.KEN_BOSS.get().create(level);
        if (boss == null) {
            return;
        }
        boss.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0);
        level.addFreshEntity(boss);
        if (boss instanceof net.minecraft.world.entity.Mob mob) {
            mob.setTarget(player);
        }
    }

    // -------------------------------------------------------------------
    // Questline
    // -------------------------------------------------------------------

    private static Questline buildQuestline() {
        List<QuestStage> stages = new ArrayList<>();

        // Quest 1 - Sur la tombe de vos parents.
        stages.add(new QuestStage(
                "graves",
                Component.translatable("quest.heroesjourney.batman.1.title"),
                Component.translatable("quest.heroesjourney.batman.1.description"),
                // The grave marker block IS the anchor: WayneManorPiece#buildGrave always places
                // it as the topmost, frontmost block of the grave (never buried under the
                // headstone), so this condition only ever needs "is this block near the player",
                // never a coordinate offset from the manor/estate origin.
                List.of(new QuestObjective("visit_graves", Component.translatable("objective.heroesjourney.batman.visit_graves"),
                        new ProximityToBlockCondition(HJItems.WAYNE_GRAVE_THOMAS.get(), 6))),
                List.of(reward(Component.translatable("reward.heroesjourney.batman.map_to_prison"), player ->
                        player.getInventory().add(TreasureMapFactory.create(player.serverLevel(), player.blockPosition(),
                                HJStructures.PRISON, Component.translatable("item.heroesjourney.treasure_map.prison"), 4000)))),
                List.of()
        ));

        // Quest 2 - Comprendre l'esprit criminel (prison escape puzzle).
        stages.add(new QuestStage(
                "prison_break",
                Component.translatable("quest.heroesjourney.batman.2.title"),
                Component.translatable("quest.heroesjourney.batman.2.description"),
                List.of(new QuestObjective("escape", Component.translatable("objective.heroesjourney.batman.escape_prison"),
                        new PrisonBreakCondition(ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, "rusty_cell_key"), Blocks.TORCH, 4))),
                List.of(reward(Component.translatable("reward.heroesjourney.batman.detective_sense"), player -> {
                    player.getData(com.heroesjourney.data.HJAttachments.HERO_DATA).getOrCreateProgress(HERO_ID)
                            .unlockAbility(BatmanAbilities.DETECTIVE_SENSE);
                })),
                List.of()
        ));

        // Quest 3 - L'entrainement de la Ligue des Ombres (3 parallel objectives).
        stages.add(new QuestStage(
                "training",
                Component.translatable("quest.heroesjourney.batman.3.title"),
                Component.translatable("quest.heroesjourney.batman.3.description"),
                List.of(
                        new QuestObjective("barehanded_kills", Component.translatable("objective.heroesjourney.batman.barehanded_kills"),
                                new KillCountCondition(HJConfig.TRAINING_BAREHANDED_KILLS.get(), true, false)),
                        new QuestObjective("run_distance", Component.translatable("objective.heroesjourney.batman.run_distance"),
                                new TravelDistanceCondition(HJConfig.TRAINING_RUN_DISTANCE.get())),
                        new QuestObjective("sneak_attacks", Component.translatable("objective.heroesjourney.batman.sneak_attacks"),
                                new KillCountCondition(HJConfig.TRAINING_SNEAK_ATTACKS.get(), false, true))
                ),
                List.of(),
                List.of()
        ));

        // Quest 4 - Le refus (boss: Ken).
        stages.add(new QuestStage(
                "the_refusal",
                Component.translatable("quest.heroesjourney.batman.4.title"),
                Component.translatable("quest.heroesjourney.batman.4.description"),
                List.of(new QuestObjective("defeat_ken", Component.translatable("objective.heroesjourney.batman.defeat_ken"),
                        new BossKillCondition("ken"))),
                List.of(reward(Component.translatable("reward.heroesjourney.batman.passives"), player -> {
                    player.getData(com.heroesjourney.data.HJAttachments.HERO_DATA).getOrCreateProgress(HERO_ID)
                            .addFlag(BatmanAbilities.FLAG_KEN_DEFEATED);
                    BatmanRecipeUnlock.grant(player);
                })),
                List.of()
        ));

        // Quest 5 - Devenir la chauve-souris (craft the full suit).
        stages.add(new QuestStage(
                "become_the_bat",
                Component.translatable("quest.heroesjourney.batman.5.title"),
                Component.translatable("quest.heroesjourney.batman.5.description"),
                List.of(new QuestObjective("wear_suit", Component.translatable("objective.heroesjourney.batman.wear_suit"),
                        new EquipmentSetCondition(List.of(
                                HJItems.BAT_COWL::get, HJItems.BAT_ARMORED_CHESTPLATE::get,
                                HJItems.BAT_LEGGINGS::get, HJItems.BAT_BOOTS::get)))),
                List.of(reward(Component.translatable("reward.heroesjourney.batman.starter_kit"), player -> {
                    give(player, HJItems.GRAPPLE_HOOK.get(), 1);
                    give(player, HJItems.BATARANG.get(), 8);
                    give(player, HJItems.SMOKE_PEBBLE.get(), 3);
                })),
                List.of(hookMessage("heroesjourney.ambient.arkham_rumours"))
        ));

        // Quest 6 - La peur elle-meme (boss: Scarecrow).
        stages.add(new QuestStage(
                "fear_itself",
                Component.translatable("quest.heroesjourney.batman.6.title"),
                Component.translatable("quest.heroesjourney.batman.6.description"),
                List.of(new QuestObjective("defeat_scarecrow", Component.translatable("objective.heroesjourney.batman.defeat_scarecrow"),
                        new BossKillCondition("scarecrow"))),
                List.of(reward(Component.translatable("reward.heroesjourney.batman.map_to_train"), player ->
                        player.getInventory().add(TreasureMapFactory.create(player.serverLevel(), player.blockPosition(),
                                HJStructures.TRAIN, Component.translatable("item.heroesjourney.treasure_map.train"), 4000)))),
                List.of()
        ));

        // Quest 7 - Le train de la peur (final boss: Henri / Ra's al Ghul).
        stages.add(new QuestStage(
                "the_train_of_fear",
                Component.translatable("quest.heroesjourney.batman.7.title"),
                Component.translatable("quest.heroesjourney.batman.7.description"),
                List.of(new QuestObjective("defeat_henri", Component.translatable("objective.heroesjourney.batman.defeat_henri"),
                        new BossKillCondition("henri"))),
                List.of(reward(Component.translatable("reward.heroesjourney.batman.wayne_manor_unlocked"), player ->
                        player.getData(com.heroesjourney.data.HJAttachments.HERO_DATA).getOrCreateProgress(HERO_ID)
                                .addFlag(BatmanAbilities.FLAG_WAYNE_MANOR_UNLOCKED))),
                List.of()
        ));

        return new Questline(HERO_ID, stages);
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

    private static StageHook hookMessage(String translationKey) {
        return (player, level) -> player.sendSystemMessage(Component.translatable(translationKey));
    }

    private static void give(ServerPlayer player, net.minecraft.world.item.Item item, int count) {
        ItemStack stack = new ItemStack(item, count);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
