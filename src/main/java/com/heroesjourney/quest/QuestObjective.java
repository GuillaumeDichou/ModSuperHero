package com.heroesjourney.quest;

import net.minecraft.network.chat.Component;

/**
 * One measurable objective within a {@link QuestStage}. Most stages have a single objective;
 * quest 3 of the Batman arc ("training") has three running in parallel.
 */
public record QuestObjective(String id, Component label, QuestCondition condition) {
}
