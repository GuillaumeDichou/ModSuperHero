package com.heroesjourney.quest.condition;

import com.heroesjourney.quest.QuestCondition;
import com.heroesjourney.quest.QuestEventContext;
import com.heroesjourney.quest.event.QuestEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

/** Complete as soon as the player has, at some point, been within {@code radius} blocks of {@code target}. */
public class ProximityToBlockCondition implements QuestCondition {

    private final Block target;
    private final int radius;

    public ProximityToBlockCondition(Block target, int radius) {
        this.target = target;
        this.radius = radius;
    }

    @Override
    public int target() {
        return 1;
    }

    @Override
    public int onEvent(QuestEventContext ctx, int currentProgress) {
        if (currentProgress >= 1 || !(ctx.event() instanceof QuestEvent.Heartbeat)) {
            return currentProgress;
        }
        return ConditionUtil.isNear(ctx.level(), ctx.player().blockPosition(), target, radius) ? 1 : currentProgress;
    }
}
