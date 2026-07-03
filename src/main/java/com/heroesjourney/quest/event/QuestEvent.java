package com.heroesjourney.quest.event;

/**
 * Marker interface for anything that can be dispatched to the active objective's
 * {@link com.heroesjourney.quest.QuestCondition}. Kept as small, self-contained records so new
 * hero content can introduce new event types without touching the dispatch code in
 * {@code QuestManager} (any condition that doesn't care about a given event type simply ignores
 * it via an {@code instanceof} check).
 */
public interface QuestEvent {

    record MobKilled(String mobTypeId, boolean bareHanded, boolean sneakAttack, boolean playerCaused) implements QuestEvent {
    }

    record ItemCrafted(String itemId) implements QuestEvent {
    }

    record ItemObtained(String itemId) implements QuestEvent {
    }

    record NpcInteracted(String npcId) implements QuestEvent {
    }

    record DialogueChoiceMade(String npcId, String choiceId) implements QuestEvent {
    }

    record BossDefeated(String bossId) implements QuestEvent {
    }

    /** Fired ~once/second per player; drives proximity/location based conditions cheaply. */
    record Heartbeat() implements QuestEvent {
    }

    /** Fired every player tick while sprinting on the ground; carries the distance moved that tick. */
    record SprintDistance(double deltaBlocks) implements QuestEvent {
    }

    record EquipmentChanged() implements QuestEvent {
    }
}
