package com.heroesjourney.hero;

import com.heroesjourney.quest.Questline;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Static description of a playable hero: its identifier, display info and questline.
 * New heroes are added by constructing one of these and calling {@link HeroRegistry#register}
 * from that hero's own content package - nothing in the generic engine needs to change.
 */
public record HeroDefinition(
        String id,
        Component displayName,
        Component shortDescription,
        ResourceLocation icon,
        Questline questline,
        HeroEffectsProvider effects
) {

    public String translationKey() {
        return "hero.heroesjourney." + id;
    }
}
