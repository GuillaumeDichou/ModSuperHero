package com.heroesjourney.content.batman;

import com.heroesjourney.data.HJAttachments;
import com.heroesjourney.data.HeroData;
import com.heroesjourney.data.HeroProgress;
import com.heroesjourney.item.HJItems;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Vanilla's recipe book "unlocked" state only governs recipe-book visibility, not whether a
 * manually-arranged crafting grid produces the item - so gating "the armor recipes are locked
 * until quest 6, the gadget recipes until quest 7" needs an explicit check. This voids the crafted
 * stack (and refunds nothing - the ingredients are the cost of trying) if the crafter hasn't
 * reached the relevant stage yet.
 */
public final class BatmanRecipeGate {

    private static final Set<Supplier<Item>> ARMOR_GATED_ITEMS = Set.of(
            HJItems.KEVLAR_FIBER::get, HJItems.MEMORY_CLOTH::get, HJItems.ELECTRONIC_COMPONENT::get,
            HJItems.BAT_COWL::get, HJItems.BAT_COMBAT_VEST::get, HJItems.BAT_CAPE::get, HJItems.BAT_ARMORED_CHESTPLATE::get,
            HJItems.BAT_LEGGINGS::get, HJItems.BAT_BOOTS::get
    );

    private static final Set<Supplier<Item>> GADGET_GATED_ITEMS = Set.of(
            HJItems.BATARANG::get, HJItems.GRAPPLE_HOOK::get, HJItems.SMOKE_PEBBLE::get
    );

    @SubscribeEvent
    public void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        HeroProgress progress = progressOf(player);
        boolean armorGated = ARMOR_GATED_ITEMS.stream().anyMatch(supplier -> event.getCrafting().is(supplier.get()));
        if (armorGated && !hasFlag(progress, BatmanAbilities.FLAG_ARMOR_RECIPES_UNLOCKED)) {
            voidCraft(event, player);
            return;
        }
        boolean gadgetGated = GADGET_GATED_ITEMS.stream().anyMatch(supplier -> event.getCrafting().is(supplier.get()));
        if (gadgetGated && !hasFlag(progress, BatmanAbilities.FLAG_GADGET_RECIPES_UNLOCKED)) {
            voidCraft(event, player);
        }
    }

    private void voidCraft(PlayerEvent.ItemCraftedEvent event, ServerPlayer player) {
        event.getCrafting().shrink(event.getCrafting().getCount());
        player.displayClientMessage(Component.translatable("heroesjourney.recipe.locked"), true);
    }

    private HeroProgress progressOf(ServerPlayer player) {
        HeroData data = player.getData(HJAttachments.HERO_DATA);
        return data.getProgress(BatmanAbilities.HERO_ID);
    }

    private boolean hasFlag(HeroProgress progress, String flag) {
        return progress != null && progress.hasFlag(flag);
    }
}
