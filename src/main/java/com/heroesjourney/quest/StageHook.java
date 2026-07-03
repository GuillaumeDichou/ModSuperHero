package com.heroesjourney.quest;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Scripted side effect run once when a stage completes, in addition to its rewards - e.g.
 * spawning a boss, despawning an NPC, sending an ambient hint message. Kept separate from
 * {@link QuestReward} because hooks are narrative/world plumbing rather than "things the player
 * receives".
 */
@FunctionalInterface
public interface StageHook {
    void run(ServerPlayer player, ServerLevel level);
}
