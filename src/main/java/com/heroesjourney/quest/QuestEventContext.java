package com.heroesjourney.quest;

import com.heroesjourney.quest.event.QuestEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Everything a {@link QuestCondition} needs to decide whether an incoming {@link QuestEvent}
 * advances it.
 */
public record QuestEventContext(ServerPlayer player, ServerLevel level, QuestEvent event) {
}
