package com.heroesjourney.quest.condition;

import com.heroesjourney.quest.QuestCondition;
import com.heroesjourney.quest.QuestEventContext;
import com.heroesjourney.quest.event.QuestEvent;

/** Counts player kills matching the given filters (bare-handed and/or sneak-attack). */
public class KillCountCondition implements QuestCondition {

    private final int count;
    private final boolean requireBareHanded;
    private final boolean requireSneakAttack;

    public KillCountCondition(int count, boolean requireBareHanded, boolean requireSneakAttack) {
        this.count = count;
        this.requireBareHanded = requireBareHanded;
        this.requireSneakAttack = requireSneakAttack;
    }

    @Override
    public int target() {
        return count;
    }

    @Override
    public int onEvent(QuestEventContext ctx, int currentProgress) {
        if (!(ctx.event() instanceof QuestEvent.MobKilled kill) || !kill.playerCaused()) {
            return currentProgress;
        }
        if (requireBareHanded && !kill.bareHanded()) {
            return currentProgress;
        }
        if (requireSneakAttack && !kill.sneakAttack()) {
            return currentProgress;
        }
        return Math.min(count, currentProgress + 1);
    }
}
