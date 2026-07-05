package com.heroesjourney.quest.condition;

import com.heroesjourney.quest.QuestCondition;
import com.heroesjourney.quest.QuestEventContext;
import com.heroesjourney.quest.event.QuestEvent;

/** Counts solved riddle-book puzzles (any of the 3 mini-game types). */
public class PuzzleCountCondition implements QuestCondition {

    private final int count;

    public PuzzleCountCondition(int count) {
        this.count = count;
    }

    @Override
    public int target() {
        return count;
    }

    @Override
    public int onEvent(QuestEventContext ctx, int currentProgress) {
        if (!(ctx.event() instanceof QuestEvent.PuzzleSolved)) {
            return currentProgress;
        }
        return Math.min(count, currentProgress + 1);
    }
}
