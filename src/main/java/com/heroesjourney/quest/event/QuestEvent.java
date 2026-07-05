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

    /** Fired ~once/second per player; drives proximity/location based conditions cheaply. */
    record Heartbeat() implements QuestEvent {
    }

    /** Fired every player tick while sprinting on the ground; carries the distance moved that tick. */
    record SprintDistance(double deltaBlocks) implements QuestEvent {
    }

    /** Fired when a nearby villager is killed by a mob (never the player) - quest 1's trigger. */
    record InnocentKilledNearby() implements QuestEvent {
    }

    /** Fired with however many of vanilla's jump stat increments happened since the last tick. */
    record PlayerJumped(int count) implements QuestEvent {
    }

    /** Fired with the swimming distance (in blocks) accrued since the last tick, from vanilla's own stat. */
    record SwimDistance(double deltaBlocks) implements QuestEvent {
    }

    /** Fired when the player solves one riddle-book mini-game, regardless of which of the 3 types. */
    record PuzzleSolved() implements QuestEvent {
    }

    record EquipmentChanged() implements QuestEvent {
    }
}
