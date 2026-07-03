package com.heroesjourney.dialogue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

/** Network-friendly, resolved view of a {@link DialogueChoiceDef} sent to the client. */
public record DialogueChoiceView(String id, Component label) {

    public static final Codec<DialogueChoiceView> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(DialogueChoiceView::id),
            ComponentSerialization.CODEC.fieldOf("label").forGetter(DialogueChoiceView::label)
    ).apply(instance, DialogueChoiceView::new));
}
