package com.heroesjourney.item.armor;

import com.heroesjourney.client.ClientHeroDataCache;
import com.heroesjourney.content.batman.BatmanAbilities;
import com.heroesjourney.data.HJAttachments;
import com.heroesjourney.data.HeroData;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

/**
 * The bat-suit chestplate: a plain {@link ArmorItem} except for granting real vanilla Elytra-style
 * gliding while worn - a single jump-while-falling starts it, and it then persists entirely on its
 * own exactly like a real Elytra (no need to hold anything), because {@link #canElytraFly} and
 * {@link #elytraFlightTick} both simply return whether Batman is the active hero: vanilla's own
 * Elytra toggle logic handles starting and sustaining the glide from there.
 * <p>
 * The one thing vanilla doesn't have on its own - a way to cancel the glide manually - is driven
 * separately, from {@code ArmorEffectsHandler#updateGlideCancel} (sneak while gliding), not from
 * here.
 * <p>
 * {@link #canElytraFly}/{@link #elytraFlightTick} are called on both logical sides, so the active
 * hero has to be read from wherever it actually lives on that side - the (server-only) attachment
 * on the server, {@link ClientHeroDataCache} (the client's synced mirror of it) on the client.
 */
public class BatChestplateItem extends ArmorItem {

    public BatChestplateItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return isBatmanActive(entity);
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        return isBatmanActive(entity);
    }

    private boolean isBatmanActive(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        HeroData data = player.level().isClientSide
                ? ClientHeroDataCache.get()
                : player.getData(HJAttachments.HERO_DATA);
        return data.hasActiveHero() && data.activeHero().equals(BatmanAbilities.HERO_ID);
    }
}
