package com.heroesjourney.quest;

import com.heroesjourney.ability.AbilityRegistry;
import com.heroesjourney.data.HJAttachments;
import com.heroesjourney.data.HeroData;
import com.heroesjourney.data.HeroProgress;
import com.heroesjourney.dialogue.DialogueChoiceDef;
import com.heroesjourney.dialogue.DialogueNode;
import com.heroesjourney.dialogue.DialogueRegistry;
import com.heroesjourney.dialogue.DialogueTree;
import com.heroesjourney.dialogue.DialogueView;
import com.heroesjourney.dialogue.NpcDefinition;
import com.heroesjourney.dialogue.DialogueChoiceView;
import com.heroesjourney.effects.HeroEffectsService;
import com.heroesjourney.entity.npc.QuestNpcEntity;
import com.heroesjourney.hero.HeroDefinition;
import com.heroesjourney.hero.HeroRegistry;
import com.heroesjourney.network.HJNetworking;
import com.heroesjourney.network.OpenDialoguePayload;
import com.heroesjourney.quest.event.QuestEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The generic quest engine: dispatches {@link QuestEvent}s to the active objective of whichever
 * hero a player currently has active, applies rewards/hooks on stage completion, and owns hero
 * switching (activation) and dialogue flow. Nothing in this class knows anything about Batman
 * specifically - all of that lives in {@code content.batman}.
 */
public final class QuestManager {

    public static final QuestManager INSTANCE = new QuestManager();

    private static final int HEARTBEAT_INTERVAL_TICKS = 20;

    private final java.util.Map<java.util.UUID, net.minecraft.world.phys.Vec3> lastPositions = new java.util.HashMap<>();

    private QuestManager() {
    }

    // -------------------------------------------------------------------
    // Vanilla event listeners -> generic QuestEvent dispatch
    // -------------------------------------------------------------------

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % HEARTBEAT_INTERVAL_TICKS == 0) {
            fireEvent(player, new QuestEvent.Heartbeat());
        }
        trackSprintDistance(player);
        HeroEffectsService.tick(player);
    }

    private void trackSprintDistance(ServerPlayer player) {
        net.minecraft.world.phys.Vec3 pos = player.position();
        net.minecraft.world.phys.Vec3 last = lastPositions.put(player.getUUID(), pos);
        if (last == null || !player.onGround()) {
            return;
        }
        if (player.isSprinting()) {
            double dx = pos.x - last.x;
            double dz = pos.z - last.z;
            double delta = Math.sqrt(dx * dx + dz * dz);
            if (delta > 0.001 && delta < 5.0) {
                fireEvent(player, new QuestEvent.SprintDistance(delta));
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        LivingEntity victim = event.getEntity();
        boolean bareHanded = player.getMainHandItem().isEmpty();
        boolean sneakAttack = player.isShiftKeyDown() && (!(victim instanceof Mob mob) || mob.getTarget() == null);
        String typeId = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString();
        fireEvent(player, new QuestEvent.MobKilled(typeId, bareHanded, sneakAttack, true));
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (event.getTarget() instanceof QuestNpcEntity npc) {
            openDialogueWith(player, npc);
        }
    }

    @SubscribeEvent
    public void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        lastPositions.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HeroData data = player.getData(HJAttachments.HERO_DATA);
            if (data.hasActiveHero()) {
                HeroProgress progress = data.getOrCreateProgress(data.activeHero());
                com.heroesjourney.effects.HeroEffectsService.applyImmediate(player, data.activeHero(), progress);
            }
            HJNetworking.syncHeroData(player);
        }
    }

    // -------------------------------------------------------------------
    // Core dispatch
    // -------------------------------------------------------------------

    public void fireEvent(ServerPlayer player, QuestEvent event) {
        HeroData data = player.getData(HJAttachments.HERO_DATA);
        if (!data.hasActiveHero()) {
            return;
        }
        String heroId = data.activeHero();
        Optional<HeroDefinition> heroOpt = HeroRegistry.get(heroId);
        if (heroOpt.isEmpty()) {
            return;
        }
        HeroDefinition hero = heroOpt.get();
        HeroProgress progress = data.getOrCreateProgress(heroId);
        Questline questline = hero.questline();
        if (questline.isComplete(progress.stageIndex())) {
            return;
        }
        QuestStage stage = questline.stageAt(progress.stageIndex()).orElse(null);
        if (stage == null) {
            return;
        }

        ServerLevel level = player.serverLevel();
        QuestEventContext ctx = new QuestEventContext(player, level, event);
        boolean anyChanged = false;
        boolean allComplete = true;
        for (QuestObjective objective : stage.objectives()) {
            String key = stage.id() + ":" + objective.id();
            int current = progress.progressFor(key);
            if (!objective.condition().isComplete(current)) {
                int updated = objective.condition().onEvent(ctx, current);
                if (updated != current) {
                    progress.setProgress(key, updated);
                    anyChanged = true;
                }
                if (!objective.condition().isComplete(updated)) {
                    allComplete = false;
                }
            }
        }

        if (allComplete) {
            completeStage(player, level, data, progress, hero, questline, stage);
        } else if (anyChanged) {
            HJNetworking.syncHeroData(player);
        }
    }

    private void completeStage(ServerPlayer player, ServerLevel level, HeroData data, HeroProgress progress,
                                HeroDefinition hero, Questline questline, QuestStage stage) {
        for (QuestReward reward : stage.rewards()) {
            reward.grant(player);
        }
        for (StageHook hook : stage.onComplete()) {
            hook.run(player, level);
        }
        progress.advanceStage();

        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.4F);
        Component notification = Component.translatable("heroesjourney.quest.completed", stage.title());
        player.connection.send(new ClientboundSetActionBarTextPacket(notification));

        HJNetworking.syncHeroData(player);

        boolean questlineDone = questline.isComplete(progress.stageIndex());
        if (questlineDone) {
            Component finished = Component.translatable("heroesjourney.quest.arc_completed", hero.displayName());
            player.connection.send(new ClientboundSetActionBarTextPacket(finished));
        }
    }

    // -------------------------------------------------------------------
    // Hero activation / switching
    // -------------------------------------------------------------------

    public void requestActivateHero(ServerPlayer player, String heroId) {
        HeroData data = player.getData(HJAttachments.HERO_DATA);
        String previous = data.activeHero();
        if (previous.equals(heroId)) {
            return;
        }
        if (!heroId.equals(HeroRegistry.NONE) && HeroRegistry.get(heroId).isEmpty()) {
            return;
        }
        HeroEffectsService.clearAll(player);
        data.setActiveHero(heroId);
        if (!heroId.equals(HeroRegistry.NONE)) {
            HeroProgress progress = data.getOrCreateProgress(heroId);
            HeroEffectsService.applyImmediate(player, heroId, progress);
        }
        HJNetworking.syncHeroData(player);
    }

    public void setHudTrackerEnabled(ServerPlayer player, boolean enabled) {
        HeroData data = player.getData(HJAttachments.HERO_DATA);
        data.setHudTrackerEnabled(enabled);
        HJNetworking.syncHeroData(player);
    }

    // -------------------------------------------------------------------
    // Dialogue
    // -------------------------------------------------------------------

    public void openDialogueWith(ServerPlayer player, QuestNpcEntity npc) {
        Optional<NpcDefinition> defOpt = DialogueRegistry.npc(npc.getNpcId());
        if (defOpt.isEmpty()) {
            return;
        }
        NpcDefinition def = defOpt.get();
        HeroData data = player.getData(HJAttachments.HERO_DATA);
        if (!def.requiredHeroId().equals(HeroRegistry.NONE) && !def.requiredHeroId().equals(data.activeHero())) {
            sendNeutralLine(player, npc, def);
            return;
        }
        fireEvent(player, new QuestEvent.NpcInteracted(def.id()));
        Optional<DialogueTree> treeOpt = DialogueRegistry.tree(def.dialogueTreeId());
        if (treeOpt.isEmpty()) {
            return;
        }
        HeroProgress progress = data.getProgress(data.activeHero());
        sendNode(player, npc, def, treeOpt.get(), treeOpt.get().resolveStart(progress));
    }

    public void handleDialogueChoice(ServerPlayer player, int npcEntityId, String choiceId) {
        Entity entity = player.serverLevel().getEntity(npcEntityId);
        if (!(entity instanceof QuestNpcEntity npc)) {
            return;
        }
        Optional<NpcDefinition> defOpt = DialogueRegistry.npc(npc.getNpcId());
        if (defOpt.isEmpty()) {
            return;
        }
        NpcDefinition def = defOpt.get();
        Optional<DialogueTree> treeOpt = DialogueRegistry.tree(def.dialogueTreeId());
        if (treeOpt.isEmpty()) {
            return;
        }
        DialogueTree tree = treeOpt.get();
        // Search every node for the matching choice id (nodes are small in number; fine to scan).
        for (DialogueNode node : tree.nodes().values()) {
            for (DialogueChoiceDef choice : node.choices()) {
                if (choice.id().equals(choiceId)) {
                    choice.onSelect().apply(player, player.serverLevel(), npc);
                    fireEvent(player, new QuestEvent.DialogueChoiceMade(def.id(), choiceId));
                    if (choice.nextNodeId() == null) {
                        return;
                    }
                    DialogueNode next = tree.node(choice.nextNodeId()).orElse(null);
                    if (next != null) {
                        sendNode(player, npc, def, tree, next);
                    }
                    return;
                }
            }
        }
    }

    private void sendNode(ServerPlayer player, QuestNpcEntity npc, NpcDefinition def, DialogueTree tree, DialogueNode node) {
        List<DialogueChoiceView> choices = new ArrayList<>();
        for (DialogueChoiceDef choice : node.choices()) {
            choices.add(new DialogueChoiceView(choice.id(), choice.label()));
        }
        DialogueView view = new DialogueView(npc.getId(), def.id(), node.id(), def.displayName(), def.texture(), node.text(), choices);
        HJNetworking.sendToPlayer(player, OpenDialoguePayload.of(view));
    }

    private void sendNeutralLine(ServerPlayer player, QuestNpcEntity npc, NpcDefinition def) {
        DialogueView view = new DialogueView(npc.getId(), def.id(), "neutral", def.displayName(), def.texture(), def.neutralLine(), List.of());
        HJNetworking.sendToPlayer(player, OpenDialoguePayload.of(view));
    }

    // -------------------------------------------------------------------
    // Abilities
    // -------------------------------------------------------------------

    public void handleUseAbility(ServerPlayer player, String abilityId) {
        HeroData data = player.getData(HJAttachments.HERO_DATA);
        if (!data.hasActiveHero()) {
            return;
        }
        HeroProgress progress = data.getProgress(data.activeHero());
        if (progress == null || !progress.hasAbility(abilityId)) {
            return;
        }
        long gameTime = player.level().getGameTime();
        if (!data.isAbilityReady(abilityId, gameTime)) {
            return;
        }
        AbilityRegistry.get(abilityId).ifPresent(ability -> {
            ability.execute().accept(player);
            data.putAbilityCooldown(abilityId, gameTime + ability.cooldownTicks().getAsInt());
        });
    }

    public void fireBossDefeated(ServerPlayer player, String bossId) {
        fireEvent(player, new QuestEvent.BossDefeated(bossId));
    }
}
