package com.heroesjourney.quest.condition;

import com.heroesjourney.quest.QuestCondition;
import com.heroesjourney.quest.QuestEventContext;
import com.heroesjourney.quest.event.QuestEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;

/**
 * Quest 2's "espionnage" objective: sneak within {@code radius} blocks of a hostile mob that
 * never notices the player, for {@code requiredConsecutiveHeartbeats} straight heartbeats (fired
 * ~once/second, so this count is effectively the number of seconds the approach must hold).
 * Each mob only ever counts once - tracked with a tag on the mob's own persistent data, so a
 * single stationary mob can't be farmed repeatedly.
 */
public class StealthApproachCondition implements QuestCondition {

    private static final String COUNTED_TAG = "heroesjourney_stealth_counted";
    private static final String STREAK_TAG = "heroesjourney_stealth_streak";

    private final int count;
    private final double radius;
    private final int requiredConsecutiveHeartbeats;

    public StealthApproachCondition(int count, double radius, int requiredConsecutiveHeartbeats) {
        this.count = count;
        this.radius = radius;
        this.requiredConsecutiveHeartbeats = requiredConsecutiveHeartbeats;
    }

    @Override
    public int target() {
        return count;
    }

    @Override
    public int onEvent(QuestEventContext ctx, int currentProgress) {
        if (currentProgress >= count || !(ctx.event() instanceof QuestEvent.Heartbeat)) {
            return currentProgress;
        }
        ServerPlayer player = ctx.player();
        if (!player.isShiftKeyDown()) {
            return currentProgress;
        }
        var box = player.getBoundingBox().inflate(radius);
        for (Mob mob : ctx.level().getEntitiesOfClass(Mob.class, box)) {
            if (!(mob instanceof Enemy) || !mob.isAlive()) {
                continue;
            }
            CompoundTag data = mob.getPersistentData();
            if (data.getBoolean(COUNTED_TAG)) {
                continue;
            }
            if (mob.getTarget() == player || !mob.closerThan(player, radius)) {
                data.putInt(STREAK_TAG, 0);
                continue;
            }
            int streak = data.getInt(STREAK_TAG) + 1;
            data.putInt(STREAK_TAG, streak);
            if (streak >= requiredConsecutiveHeartbeats) {
                data.putBoolean(COUNTED_TAG, true);
                return Math.min(count, currentProgress + 1);
            }
        }
        return currentProgress;
    }
}
