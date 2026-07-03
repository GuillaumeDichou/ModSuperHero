package com.heroesjourney.dialogue;

import com.heroesjourney.entity.npc.QuestNpcEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * One selectable answer in a {@link DialogueNode}. {@code onSelect} runs server-side the moment
 * the player picks it (e.g. to fire a {@code QuestEvent.DialogueChoiceMade}, or - for "Le refus"
 * in quest 4 - to turn the NPC hostile); {@code nextNodeId} is {@code null} to end the
 * conversation.
 */
public record DialogueChoiceDef(String id, Component label, String nextNodeId, DialogueEffect onSelect) {

    @FunctionalInterface
    public interface DialogueEffect {
        void apply(ServerPlayer player, ServerLevel level, QuestNpcEntity npc);

        DialogueEffect NONE = (player, level, npc) -> {
        };
    }

    public static DialogueChoiceDef of(String id, Component label, String nextNodeId) {
        return new DialogueChoiceDef(id, label, nextNodeId, DialogueEffect.NONE);
    }
}
