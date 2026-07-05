package com.heroesjourney.quest.condition;

import com.heroesjourney.quest.QuestCondition;
import com.heroesjourney.quest.QuestEventContext;
import com.heroesjourney.quest.event.QuestEvent;

/** Complete as soon as {@link QuestEvent.InnocentKilledNearby} is dispatched to the player. */
public class WitnessDeathCondition implements QuestCondition {

    @Override
    public int target() {
        return 1;
    }

    @Override
    public int onEvent(QuestEventContext ctx, int currentProgress) {
        if (ctx.event() instanceof QuestEvent.InnocentKilledNearby) {
            return 1;
        }
        return currentProgress;
    }
}
