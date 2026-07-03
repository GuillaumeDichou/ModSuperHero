package com.heroesjourney.quest;

import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * One step of a {@link Questline}. A stage is complete once every one of its
 * {@link QuestObjective}s is complete; completion grants every {@link QuestReward} and runs
 * every {@link StageHook}, then advances the player to the next stage.
 */
public record QuestStage(
        String id,
        Component title,
        Component description,
        List<QuestObjective> objectives,
        List<QuestReward> rewards,
        List<StageHook> onComplete
) {

    public QuestStage {
        objectives = List.copyOf(objectives);
        rewards = List.copyOf(rewards);
        onComplete = List.copyOf(onComplete);
    }
}
