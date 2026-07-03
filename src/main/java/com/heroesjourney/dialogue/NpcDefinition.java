package com.heroesjourney.dialogue;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Static description of a quest-giver NPC "role" (Ken, Henri, a prisoner, ...). Multiple entity
 * instances in the world can share the same id (there are several prisoners, for instance);
 * {@link #requiredHeroId} is what makes an NPC stay silent to players who haven't activated the
 * matching hero.
 */
public record NpcDefinition(
        String id,
        Component displayName,
        ResourceLocation texture,
        String requiredHeroId,
        String dialogueTreeId,
        Component neutralLine
) {
}
