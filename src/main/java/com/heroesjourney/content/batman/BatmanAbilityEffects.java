package com.heroesjourney.content.batman;

import com.heroesjourney.config.HJConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

/**
 * "Detective sense": rather than a client-side outline shader (fragile across renderer
 * versions), chests within range are revealed with a short burst of particles sent only to the
 * activating player, repeated for the configured duration.
 */
public final class BatmanAbilityEffects {

    private BatmanAbilityEffects() {
    }

    public static void detectiveSense(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        int radius = HJConfig.DETECTIVE_SENSE_RADIUS.get();
        int durationTicks = HJConfig.DETECTIVE_SENSE_DURATION_TICKS.get();
        BlockPos center = player.blockPosition();
        java.util.List<BlockPos> chestPositions = new java.util.ArrayList<>();
        // Scan every loaded chunk within range for chest block entities.
        int chunkRadius = (radius / 16) + 1;
        for (int cx = -chunkRadius; cx <= chunkRadius; cx++) {
            for (int cz = -chunkRadius; cz <= chunkRadius; cz++) {
                var chunk = level.getChunkSource().getChunkNow((center.getX() >> 4) + cx, (center.getZ() >> 4) + cz);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be instanceof ChestBlockEntity && be.getBlockPos().closerThan(center, radius) && !chestPositions.contains(be.getBlockPos())) {
                        chestPositions.add(be.getBlockPos());
                    }
                }
            }
        }

        int pulses = Math.max(1, durationTicks / 10);
        for (int i = 0; i < pulses; i++) {
            int delay = i * 10;
            level.getServer().tell(new net.minecraft.server.TickTask(level.getServer().getTickCount() + delay, () -> {
                for (BlockPos pos : chestPositions) {
                    level.sendParticles(player, ParticleTypes.END_ROD, false,
                            pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, 3, 0.15, 0.15, 0.15, 0.0);
                }
            }));
        }
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("heroesjourney.ability.detective_sense.used"), true);
    }
}
