package com.heroesjourney.dialogue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

/**
 * Fully resolved snapshot of "what the dialogue screen should currently show", sent from server
 * to client. Resolving it server-side (rather than sending the whole {@link DialogueTree} once)
 * keeps all quest-state-dependent branching logic server-authoritative.
 */
public record DialogueView(
        int npcEntityId,
        String npcId,
        String nodeId,
        Component speakerName,
        ResourceLocation speakerTexture,
        Component text,
        List<DialogueChoiceView> choices
) {

    public DialogueView {
        choices = List.copyOf(choices);
    }

    public boolean terminal() {
        return choices.isEmpty();
    }

    public static final Codec<DialogueView> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("npcEntityId").forGetter(DialogueView::npcEntityId),
            Codec.STRING.fieldOf("npcId").forGetter(DialogueView::npcId),
            Codec.STRING.fieldOf("nodeId").forGetter(DialogueView::nodeId),
            ComponentSerialization.CODEC.fieldOf("speakerName").forGetter(DialogueView::speakerName),
            ResourceLocation.CODEC.fieldOf("speakerTexture").forGetter(DialogueView::speakerTexture),
            ComponentSerialization.CODEC.fieldOf("text").forGetter(DialogueView::text),
            DialogueChoiceView.CODEC.listOf().fieldOf("choices").forGetter(DialogueView::choices)
    ).apply(instance, DialogueView::new));
}
