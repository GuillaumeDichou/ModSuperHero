package com.heroesjourney.quest;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Something granted to a player when a {@link QuestStage} is completed. Rewards are only ever
 * granted by {@code QuestManager} on stage validation - never by killing a boss directly, per the
 * design brief (bosses are respawnable and must not be a repeatable reward source).
 */
public interface QuestReward {

    void grant(ServerPlayer player);

    /** Short human readable summary shown in the hero detail screen next to the stage. */
    Component summary();
}
