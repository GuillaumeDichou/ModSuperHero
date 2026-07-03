package com.heroesjourney.quest;

import net.minecraft.network.chat.Component;

/**
 * The validation logic for a single {@link QuestObjective}. Implementations are small, stateless
 * strategy objects: all mutable per-player state lives in the {@code int} progress counter that
 * the engine stores for that objective, which is passed in and returned.
 * <p>
 * New condition types (structures, dialogue, custom hero mechanics, ...) are added by
 * implementing this interface - the generic engine only ever calls {@link #onEvent} and
 * {@link #target()}.
 */
public interface QuestCondition {

    /** Progress value considered "complete". Use 1 for simple boolean conditions. */
    int target();

    /**
     * Process one dispatched event and return the new progress value (0..target()).
     * Implementations that don't care about {@code ctx.event()}'s type should just return
     * {@code currentProgress} unchanged.
     */
    int onEvent(QuestEventContext ctx, int currentProgress);

    default boolean isComplete(int progress) {
        return progress >= target();
    }

    /** Short progress label shown in the hero detail screen, e.g. "12 / 30". */
    default Component describeProgress(int progress) {
        if (target() <= 1) {
            return Component.empty();
        }
        return Component.literal(Math.min(progress, target()) + " / " + target());
    }
}
