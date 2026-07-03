package com.heroesjourney.content.batman;

import com.heroesjourney.HeroesJourney;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

/** Grants the hidden "unlock every bat-suit/gadget recipe" advancement created for quest 4's reward. */
final class BatmanRecipeUnlock {

    private static final ResourceLocation ADVANCEMENT_ID =
            ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, "recipes/bat_gear");

    private BatmanRecipeUnlock() {
    }

    static void grant(ServerPlayer player) {
        AdvancementHolder advancement = player.server.getAdvancements().get(ADVANCEMENT_ID);
        if (advancement == null) {
            return;
        }
        PlayerAdvancements advancements = player.getAdvancements();
        for (String criterion : advancement.value().criteria().keySet()) {
            advancements.award(advancement, criterion);
        }
    }
}
