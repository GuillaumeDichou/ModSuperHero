package com.heroesjourney.quest.condition;

import com.heroesjourney.quest.QuestCondition;
import com.heroesjourney.quest.QuestEventContext;
import com.heroesjourney.quest.event.QuestEvent;

/** Counts jumps via vanilla's own jump stat (see {@code QuestManager#trackVanillaStatDelta}). */
public class JumpCountCondition implements QuestCondition {

    private final int count;

    public JumpCountCondition(int count) {
        this.count = count;
    }

    @Override
    public int target() {
        return count;
    }

    @Override
    public int onEvent(QuestEventContext ctx, int currentProgress) {
        if (!(ctx.event() instanceof QuestEvent.PlayerJumped jumped)) {
            return currentProgress;
        }
        return Math.min(count, currentProgress + jumped.count());
    }
}
