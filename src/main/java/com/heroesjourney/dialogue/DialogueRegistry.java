package com.heroesjourney.dialogue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Simple in-memory registries for {@link NpcDefinition}s and {@link DialogueTree}s, mirroring {@code HeroRegistry}. */
public final class DialogueRegistry {

    private static final Map<String, NpcDefinition> NPCS = new HashMap<>();
    private static final Map<String, DialogueTree> TREES = new HashMap<>();

    private DialogueRegistry() {
    }

    public static void clear() {
        NPCS.clear();
        TREES.clear();
    }

    public static void registerNpc(NpcDefinition definition) {
        NPCS.put(definition.id(), definition);
    }

    public static void registerTree(DialogueTree tree) {
        TREES.put(tree.npcId(), tree);
    }

    public static Optional<NpcDefinition> npc(String id) {
        return Optional.ofNullable(NPCS.get(id));
    }

    public static Optional<DialogueTree> tree(String npcId) {
        return Optional.ofNullable(TREES.get(npcId));
    }
}
