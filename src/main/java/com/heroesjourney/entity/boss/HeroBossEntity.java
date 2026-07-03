package com.heroesjourney.entity.boss;

import com.heroesjourney.config.HJConfig;
import com.heroesjourney.quest.QuestManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ServerBossEvent;

/**
 * Common base for every hero boss (Ken, Scarecrow, Henri, and future heroes' bosses): owns a
 * bossbar, tracks which players contributed meaningful damage (for quest credit - killing a boss
 * grants no reward directly, {@code QuestManager} is only ever notified so it can validate the
 * relevant objective), and schedules a respawn at its home {@link BossSpawnerBlockEntity} when
 * killed.
 */
public abstract class HeroBossEntity extends Monster {

    private final ServerBossEvent bossEvent;
    private final Map<UUID, Long> recentDamagers = new HashMap<>();
    @Nullable
    private BlockPos homeSpawner;

    protected HeroBossEntity(EntityType<? extends Monster> type, Level level, BossEvent.BossBarColor color) {
        super(type, level);
        this.bossEvent = new ServerBossEvent(this.getDisplayName(), color, BossEvent.BossBarOverlay.PROGRESS);
        this.xpReward = 0;
    }

    public abstract String bossId();

    public void setHomeSpawner(BlockPos pos) {
        this.homeSpawner = pos;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && source.getEntity() instanceof ServerPlayer player) {
            recentDamagers.put(player.getUUID(), this.level().getGameTime());
        }
        return result;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.tickCount % 20 == 0) {
            updateBossBarViewers();
        }
    }

    private void updateBossBarViewers() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        double range = 64.0D;
        for (ServerPlayer player : serverLevel.players()) {
            boolean near = player.distanceToSqr(this) <= range * range;
            if (near) {
                bossEvent.addPlayer(player);
            } else {
                bossEvent.removePlayer(player);
            }
        }
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            long window = HJConfig.BOSS_CREDIT_DAMAGE_WINDOW_TICKS.get();
            long now = serverLevel.getGameTime();
            for (Map.Entry<UUID, Long> entry : recentDamagers.entrySet()) {
                if (now - entry.getValue() > window) {
                    continue;
                }
                if (serverLevel.getServer().getPlayerList().getPlayer(entry.getKey()) instanceof ServerPlayer contributor) {
                    QuestManager.INSTANCE.fireBossDefeated(contributor, bossId());
                }
            }
            if (source.getEntity() instanceof ServerPlayer killer && (now - recentDamagers.getOrDefault(killer.getUUID(), -1L)) > window) {
                QuestManager.INSTANCE.fireBossDefeated(killer, bossId());
            }
            bossEvent.removeAllPlayers();
            if (homeSpawner != null) {
                BlockEntity be = serverLevel.getBlockEntity(homeSpawner);
                if (be instanceof BossSpawnerBlockEntity spawner) {
                    spawner.scheduleRespawn(now + HJConfig.BOSS_RESPAWN_DELAY_TICKS.get());
                }
            }
        }
        super.die(source);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (homeSpawner != null) {
            tag.putInt("HomeX", homeSpawner.getX());
            tag.putInt("HomeY", homeSpawner.getY());
            tag.putInt("HomeZ", homeSpawner.getZ());
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HomeX")) {
            homeSpawner = new BlockPos(tag.getInt("HomeX"), tag.getInt("HomeY"), tag.getInt("HomeZ"));
        }
    }
}
