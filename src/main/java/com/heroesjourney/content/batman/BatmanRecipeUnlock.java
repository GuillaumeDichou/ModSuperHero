package com.heroesjourney.content.batman;

import com.heroesjourney.HeroesJourney;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

/**
 * Grants one of the two hidden "unlock these recipes" advancements: {@code recipes/bat_armor}
 * (quest 6 reward) or {@code recipes/bat_gadgets} (quest 7 reward).
 */
final class BatmanRecipeUnlock {

    static final ResourceLocation ARMOR_ADVANCEMENT_ID =
            ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, "recipes/bat_armor");
    static final ResourceLocation GADGETS_ADVANCEMENT_ID =
            ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, "recipes/bat_gadgets");

    private BatmanRecipeUnlock() {
    }

    static void grant(ServerPlayer player, ResourceLocation advancementId) {
        AdvancementHolder advancement = player.server.getAdvancements().get(advancementId);
        if (advancement == null) {
            return;
        }
        PlayerAdvancements advancements = player.getAdvancements();
        for (String criterion : advancement.value().criteria().keySet()) {
            advancements.award(advancement, criterion);
        }
    }
}
