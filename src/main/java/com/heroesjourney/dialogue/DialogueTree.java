package com.heroesjourney.dialogue;

import com.heroesjourney.data.HeroProgress;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public record DialogueTree(String npcId, String startNodeId, Map<String, DialogueNode> nodes, Function<HeroProgress, String> startNodeSelector) {

    public DialogueTree {
        nodes = Map.copyOf(nodes);
    }

    public static DialogueTree simple(String npcId, String startNodeId, Map<String, DialogueNode> nodes) {
        return new DialogueTree(npcId, startNodeId, nodes, progress -> startNodeId);
    }

    public Optional<DialogueNode> node(String id) {
        return Optional.ofNullable(nodes.get(id));
    }

    public DialogueNode start() {
        return nodes.get(startNodeId);
    }

    /** Picks the start node based on the player's current progress on this hero's questline (defaults to the fixed start node). */
    public DialogueNode resolveStart(HeroProgress progress) {
        String id = progress == null ? startNodeId : startNodeSelector.apply(progress);
        DialogueNode node = nodes.get(id);
        return node != null ? node : nodes.get(startNodeId);
    }
}
