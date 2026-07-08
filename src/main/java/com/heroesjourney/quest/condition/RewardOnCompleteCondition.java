package com.heroesjourney.quest.condition;

import com.heroesjourney.quest.QuestCondition;
import com.heroesjourney.quest.QuestEventContext;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Wraps another condition and runs {@code onComplete} exactly once, the instant its progress
 * crosses into "complete" - lets one objective within a multi-objective stage grant its own
 * reward without waiting for the rest of the stage to finish, since the engine otherwise only
 * grants {@link com.heroesjourney.quest.QuestReward}s once every objective of a stage is done.
 */
public class RewardOnCompleteCondition implements QuestCondition {

    private final QuestCondition delegate;
    private final Consumer<ServerPlayer> onComplete;
    private final Component rewardSummary;

    public RewardOnCompleteCondition(QuestCondition delegate, Component rewardSummary, Consumer<ServerPlayer> onComplete) {
        this.delegate = delegate;
        this.rewardSummary = rewardSummary;
        this.onComplete = onComplete;
    }

    /** Shown in the UI next to this objective, so a per-objective reward (unlike stage-level {@link com.heroesjourney.quest.QuestReward}s) is visible too. */
    public Component rewardSummary() {
        return rewardSummary;
    }

    @Override
    public int target() {
        return delegate.target();
    }

    @Override
    public int onEvent(QuestEventContext ctx, int currentProgress) {
        int updated = delegate.onEvent(ctx, currentProgress);
        if (!delegate.isComplete(currentProgress) && delegate.isComplete(updated)) {
            onComplete.accept(ctx.player());
        }
        return updated;
    }

    @Override
    public boolean isComplete(int progress) {
        return delegate.isComplete(progress);
    }

    @Override
    public Component describeProgress(int progress) {
        return delegate.describeProgress(progress);
    }
}
