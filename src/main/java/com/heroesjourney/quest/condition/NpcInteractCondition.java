package com.heroesjourney.quest.condition;

import com.heroesjourney.quest.QuestCondition;
import com.heroesjourney.quest.QuestEventContext;
import com.heroesjourney.quest.event.QuestEvent;

public class NpcInteractCondition implements QuestCondition {

    private final String npcId;

    public NpcInteractCondition(String npcId) {
        this.npcId = npcId;
    }

    @Override
    public int target() {
        return 1;
    }

    @Override
    public int onEvent(QuestEventContext ctx, int currentProgress) {
        if (ctx.event() instanceof QuestEvent.NpcInteracted interacted && interacted.npcId().equals(npcId)) {
            return 1;
        }
        return currentProgress;
    }
}
