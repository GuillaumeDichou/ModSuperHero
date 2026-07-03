package com.heroesjourney.content.batman;

import com.heroesjourney.data.HJAttachments;
import com.heroesjourney.data.HeroData;
import com.heroesjourney.data.HeroProgress;
import com.heroesjourney.item.HJItems;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Vanilla's recipe book "unlocked" state only governs recipe-book visibility, not whether a
 * manually-arranged crafting grid produces the item - so gating "the bat-suit/gadget recipes are
 * locked until quest 4" needs an explicit check. This voids the crafted stack (and refunds
 * nothing - the ingredients are the cost of trying) if the crafter hasn't defeated Ken yet.
 */
public final class BatmanRecipeGate {

    private static final Set<java.util.function.Supplier<Item>> GATED_ITEMS = Set.of(
            HJItems.KEVLAR_FIBER::get, HJItems.MEMORY_CLOTH::get, HJItems.ELECTRONIC_COMPONENT::get,
            HJItems.BAT_COWL::get, HJItems.BAT_COMBAT_VEST::get, HJItems.BAT_CAPE::get, HJItems.BAT_ARMORED_CHESTPLATE::get,
            HJItems.BAT_LEGGINGS::get, HJItems.BAT_BOOTS::get,
            HJItems.BATARANG::get, HJItems.GRAPPLE_HOOK::get, HJItems.SMOKE_PEBBLE::get
    );

    @SubscribeEvent
    public void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        boolean gated = GATED_ITEMS.stream().anyMatch(supplier -> event.getCrafting().is(supplier.get()));
        if (!gated || isUnlocked(player)) {
            return;
        }
        event.getCrafting().shrink(event.getCrafting().getCount());
        player.displayClientMessage(Component.translatable("heroesjourney.recipe.locked"), true);
    }

    private boolean isUnlocked(ServerPlayer player) {
        HeroData data = player.getData(HJAttachments.HERO_DATA);
        HeroProgress progress = data.getProgress(BatmanAbilities.HERO_ID);
        return progress != null && progress.hasFlag(BatmanAbilities.FLAG_KEN_DEFEATED);
    }
}
