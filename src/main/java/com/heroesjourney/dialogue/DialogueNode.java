package com.heroesjourney.dialogue;

import java.util.List;
import net.minecraft.network.chat.Component;

public record DialogueNode(String id, Component text, List<DialogueChoiceDef> choices) {

    public DialogueNode {
        choices = List.copyOf(choices);
    }

    public boolean isTerminal() {
        return choices.isEmpty();
    }
}
