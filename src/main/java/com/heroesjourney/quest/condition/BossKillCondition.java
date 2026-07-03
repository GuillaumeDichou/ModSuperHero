package com.heroesjourney.quest.condition;

import com.heroesjourney.quest.QuestCondition;
import com.heroesjourney.quest.QuestEventContext;
import com.heroesjourney.quest.event.QuestEvent;

public class BossKillCondition implements QuestCondition {

    private final String bossId;

    public BossKillCondition(String bossId) {
        this.bossId = bossId;
    }

    @Override
    public int target() {
        return 1;
    }

    @Override
    public int onEvent(QuestEventContext ctx, int currentProgress) {
        if (ctx.event() instanceof QuestEvent.BossDefeated defeated && defeated.bossId().equals(bossId)) {
            return 1;
        }
        return currentProgress;
    }
}
