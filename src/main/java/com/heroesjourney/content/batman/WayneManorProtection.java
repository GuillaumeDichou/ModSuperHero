package com.heroesjourney.content.batman;

import com.heroesjourney.config.HJConfig;
import com.heroesjourney.data.HJAttachments;
import com.heroesjourney.data.HeroData;
import com.heroesjourney.data.HeroProgress;
import com.heroesjourney.structure.HJStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

/**
 * Implements the design brief's "Wayne Manor is fully protected until quest 7" rule. The flag is
 * per-player (see {@link BatmanAbilities#FLAG_WAYNE_MANOR_UNLOCKED}), so protection is evaluated
 * against whichever players are actually near the block/explosion in question, not globally.
 */
public final class WayneManorProtection {

    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (isProtectedFor(level, event.getPos(), event.getPlayer() instanceof ServerPlayer sp ? sp : null)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        ServerPlayer player = event.getEntity() instanceof ServerPlayer sp ? sp : null;
        if (isProtectedFor(level, event.getPos(), player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos center = BlockPos.containing(event.getExplosion().center());
        if (!inWayneManor(level, center)) {
            return;
        }
        boolean anyUnprotectedNearby = level.players().stream()
                .filter(p -> p.blockPosition().closerThan(center, 48))
                .anyMatch(p -> !hasFlag(p));
        if (anyUnprotectedNearby) {
            event.getAffectedBlocks().clear();
        }
    }

    @SubscribeEvent
    public void onMobSpawn(MobSpawnEvent.PositionCheck event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (event.getEntity().getType().getCategory() != net.minecraft.world.entity.MobCategory.MONSTER) {
            return;
        }
        BlockPos pos = BlockPos.containing(event.getX(), event.getY(), event.getZ());
        if (!inWayneManor(level, pos)) {
            return;
        }
        ServerPlayer nearest = level.getNearestPlayer(event.getX(), event.getY(), event.getZ(), 32, false);
        if (nearest != null && !hasFlag(nearest)) {
            event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
        }
    }

    private boolean isProtectedFor(ServerLevel level, BlockPos pos, ServerPlayer actingPlayer) {
        if (!inWayneManor(level, pos)) {
            return false;
        }
        if (actingPlayer != null) {
            return !hasFlag(actingPlayer);
        }
        return level.players().stream().anyMatch(p -> !hasFlag(p) && p.blockPosition().closerThan(pos, HJConfig.WAYNE_MANOR_PROTECTION_RADIUS.get()));
    }

    private boolean inWayneManor(ServerLevel level, BlockPos pos) {
        if (!HJConfig.WAYNE_MANOR_PROTECTION_ENABLED.get()) {
            return false;
        }
        return level.structureManager().getStructureWithPieceAt(pos, HJStructures.WAYNE_MANOR).isValid();
    }

    private boolean hasFlag(ServerPlayer player) {
        HeroData data = player.getData(HJAttachments.HERO_DATA);
        HeroProgress progress = data.getProgress(BatmanAbilities.HERO_ID);
        return progress != null && progress.hasFlag(BatmanAbilities.FLAG_WAYNE_MANOR_UNLOCKED);
    }
}
