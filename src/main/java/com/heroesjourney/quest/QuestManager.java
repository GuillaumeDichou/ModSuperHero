package com.heroesjourney.quest;

import com.heroesjourney.ability.AbilityRegistry;
import com.heroesjourney.data.HJAttachments;
import com.heroesjourney.data.HeroData;
import com.heroesjourney.data.HeroProgress;
import com.heroesjourney.effects.HeroEffectsService;
import com.heroesjourney.hero.HeroDefinition;
import com.heroesjourney.hero.HeroRegistry;
import com.heroesjourney.network.HJNetworking;
import com.heroesjourney.quest.event.QuestEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The generic quest engine: dispatches {@link QuestEvent}s to the active objective of whichever
 * hero a player currently has active, applies rewards/hooks on stage completion, and owns hero
 * switching (activation). Nothing in this class knows anything about Batman specifically - all of
 * that lives in {@code content.batman}.
 */
public final class QuestManager {

    public static final QuestManager INSTANCE = new QuestManager();

    private static final int HEARTBEAT_INTERVAL_TICKS = 20;

    private final Map<UUID, Integer> lastSprintStat = new HashMap<>();
    private final Map<UUID, Integer> lastJumpStat = new HashMap<>();
    private final Map<UUID, Integer> lastSwimStat = new HashMap<>();

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
        // Distance/count objectives are driven by vanilla's own stats (the same ones behind the
        // vanilla "distances" advancements/statistics screen) rather than hand-rolled tick
        // detection: vanilla already tracks these reliably, so there's no risk of under/over
        // counting from e.g. a missed ground-state transition. All three are stored by vanilla in
        // centimetres, hence the /100.0 to recover blocks.
        trackVanillaStatDelta(player, lastSprintStat, Stats.CUSTOM.get(Stats.SPRINT_ONE_CM),
                delta -> new QuestEvent.SprintDistance(delta / 100.0));
        trackVanillaStatDelta(player, lastJumpStat, Stats.CUSTOM.get(Stats.JUMP),
                delta -> new QuestEvent.PlayerJumped(delta));
        trackVanillaStatDelta(player, lastSwimStat, Stats.CUSTOM.get(Stats.SWIM_ONE_CM),
                delta -> new QuestEvent.SwimDistance(delta / 100.0));
        HeroEffectsService.tick(player);
    }

    /**
     * Fires a {@link QuestEvent} for the amount a vanilla custom stat increased since the last
     * tick this player was seen. Deltas (not a baseline captured at objective-start) are used so
     * neither a relog nor an objective that was already complete before this stat existed ever
     * misfires a huge one-off jump.
     */
    private void trackVanillaStatDelta(ServerPlayer player, Map<UUID, Integer> lastSeen,
                                        net.minecraft.stats.Stat<net.minecraft.resources.ResourceLocation> stat,
                                        java.util.function.Function<Integer, QuestEvent> eventFactory) {
        int current = player.getStats().getValue(stat);
        Integer last = lastSeen.put(player.getUUID(), current);
        if (last != null && current > last) {
            fireEvent(player, eventFactory.apply(current - last));
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            boolean bareHanded = player.getMainHandItem().isEmpty();
            boolean sneakAttack = player.isShiftKeyDown() && (!(victim instanceof Mob mob) || mob.getTarget() == null);
            String typeId = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString();
            fireEvent(player, new QuestEvent.MobKilled(typeId, bareHanded, sneakAttack, true));
            return;
        }
        onNonPlayerDeath(event, victim);
    }

    /**
     * Quest-1's trigger: a villager dies to a mob (never the player, a fall, fire/lava, or
     * drowning - none of those have a {@link LivingEntity} as the damage source's entity, so that
     * single check covers every exclusion the design calls for) while a Batman-hero player is
     * nearby to witness it.
     */
    private void onNonPlayerDeath(LivingDeathEvent event, LivingEntity victim) {
        if (!(victim instanceof Villager)) {
            return;
        }
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof LivingEntity attacker) || attacker instanceof Player) {
            return;
        }
        if (!(victim.level() instanceof ServerLevel level)) {
            return;
        }
        int radius = com.heroesjourney.config.HJConfig.ORIGIN_VILLAGER_WITNESS_RADIUS.get();
        net.minecraft.core.BlockPos pos = victim.blockPosition();
        for (ServerPlayer nearby : level.players()) {
            if (nearby.blockPosition().closerThan(pos, radius)) {
                fireEvent(nearby, new QuestEvent.InnocentKilledNearby());
            }
        }
    }

    @SubscribeEvent
    public void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        lastSprintStat.remove(event.getEntity().getUUID());
        lastJumpStat.remove(event.getEntity().getUUID());
        lastSwimStat.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HeroData data = player.getData(HJAttachments.HERO_DATA);
            if (data.hasActiveHero()) {
                HeroProgress progress = data.getOrCreateProgress(data.activeHero());
                HeroEffectsService.applyImmediate(player, data.activeHero(), progress);
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

    public void firePuzzleSolved(ServerPlayer player) {
        fireEvent(player, new QuestEvent.PuzzleSolved());
    }
}
