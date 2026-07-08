package com.heroesjourney.quest.condition;

import com.heroesjourney.quest.QuestCondition;
import com.heroesjourney.quest.QuestEventContext;
import com.heroesjourney.quest.event.QuestEvent;
import net.minecraft.network.chat.Component;

/**
 * Accumulates swimming distance (from vanilla's swim stats) until {@code targetBlocks} is
 * reached. Progress is stored in centimetres, not blocks: each tick's delta is a fraction of a
 * block, so rounding to whole blocks every tick would round every single delta down to 0 and
 * progress would never move at all.
 */
public class SwimDistanceCondition implements QuestCondition {

    private static final int CM_PER_BLOCK = 100;

    private final int targetBlocks;

    public SwimDistanceCondition(int targetBlocks) {
        this.targetBlocks = targetBlocks;
    }

    @Override
    public int target() {
        return targetBlocks * CM_PER_BLOCK;
    }

    @Override
    public int onEvent(QuestEventContext ctx, int currentProgress) {
        if (!(ctx.event() instanceof QuestEvent.SwimDistance swim)) {
            return currentProgress;
        }
        int updated = currentProgress + (int) Math.round(swim.deltaBlocks() * CM_PER_BLOCK);
        return Math.min(target(), updated);
    }

    @Override
    public Component describeProgress(int progress) {
        int blocks = Math.min(progress, target()) / CM_PER_BLOCK;
        return Component.literal(blocks + " / " + targetBlocks + " blocs");
    }
}
