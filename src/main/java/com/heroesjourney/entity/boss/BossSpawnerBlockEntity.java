package com.heroesjourney.entity.boss;

import com.heroesjourney.config.HJConfig;
import com.heroesjourney.entity.HJEntities;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Marks a boss "anchor point" placed inside a structure's arena. Tracks the current boss
 * instance (if alive) and, once it dies, when to respawn a fresh one at this exact spot - this is
 * what makes bosses respawnable per-structure-instance for multiplayer, as required by the
 * design brief.
 */
public class BossSpawnerBlockEntity extends BlockEntity {

    private String bossId = "";
    private UUID trackedBoss;
    private long respawnAtGameTime = -1L;

    public BossSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(HJEntities.BOSS_SPAWNER_BE.get(), pos, state);
    }

    public void configure(String bossId) {
        this.bossId = bossId;
        setChanged();
    }

    public void notifyBossSpawned(UUID uuid) {
        this.trackedBoss = uuid;
        this.respawnAtGameTime = -1L;
        setChanged();
    }

    public void scheduleRespawn(long readyAtGameTime) {
        this.trackedBoss = null;
        this.respawnAtGameTime = readyAtGameTime;
        setChanged();
    }

    public static void serverTick(net.minecraft.world.level.Level rawLevel, BlockPos pos, BlockState state, BossSpawnerBlockEntity be) {
        if (!(rawLevel instanceof ServerLevel level)) {
            return;
        }
        if (be.bossId.isEmpty()) {
            return;
        }
        if (be.trackedBoss != null) {
            if (level.getEntity(be.trackedBoss) == null) {
                // The boss is gone but we never got a die() callback (e.g. forced /kill); allow a respawn.
                be.trackedBoss = null;
                if (be.respawnAtGameTime < 0) {
                    be.respawnAtGameTime = level.getGameTime() + HJConfig.BOSS_RESPAWN_DELAY_TICKS.get();
                }
            }
            return;
        }
        if (be.respawnAtGameTime >= 0 && level.getGameTime() >= be.respawnAtGameTime) {
            EntityType<?> type = BossRegistry.entityType(be.bossId).orElse(null);
            if (type != null) {
                Entity spawned = type.create(level);
                if (spawned instanceof HeroBossEntity boss) {
                    boss.setHomeSpawner(pos);
                    boss.moveTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0);
                    level.addFreshEntity(boss);
                    be.notifyBossSpawned(boss.getUUID());
                }
            }
            be.respawnAtGameTime = -1L;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("bossId", bossId);
        tag.putLong("respawnAtGameTime", respawnAtGameTime);
        if (trackedBoss != null) {
            tag.putUUID("trackedBoss", trackedBoss);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.bossId = tag.contains("bossId") ? tag.getString("bossId") : "";
        this.respawnAtGameTime = tag.contains("respawnAtGameTime") ? tag.getLong("respawnAtGameTime") : -1L;
        this.trackedBoss = tag.hasUUID("trackedBoss") ? tag.getUUID("trackedBoss") : null;
    }
}
