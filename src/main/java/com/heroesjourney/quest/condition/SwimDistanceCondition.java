package com.heroesjourney.quest.condition;

import com.heroesjourney.quest.QuestCondition;
import com.heroesjourney.quest.QuestEventContext;
import com.heroesjourney.quest.event.QuestEvent;
import net.minecraft.network.chat.Component;

/** Accumulates swimming distance (in blocks, from vanilla's swim stat) until {@code targetBlocks} is reached. */
public class SwimDistanceCondition implements QuestCondition {

    private final int targetBlocks;

    public SwimDistanceCondition(int targetBlocks) {
        this.targetBlocks = targetBlocks;
    }

    @Override
    public int target() {
        return targetBlocks;
    }

    @Override
    public int onEvent(QuestEventContext ctx, int currentProgress) {
        if (!(ctx.event() instanceof QuestEvent.SwimDistance swim)) {
            return currentProgress;
        }
        int updated = currentProgress + (int) Math.round(swim.deltaBlocks());
        return Math.min(targetBlocks, updated);
    }

    @Override
    public Component describeProgress(int progress) {
        return Component.literal(Math.min(progress, targetBlocks) + " / " + targetBlocks + " blocs");
    }
}
