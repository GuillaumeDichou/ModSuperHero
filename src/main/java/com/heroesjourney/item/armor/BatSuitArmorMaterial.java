package com.heroesjourney.item.armor;

import com.heroesjourney.HeroesJourney;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Shared armor material for the whole bat-suit (cowl, chestplate, leggings, boots). Sits between
 * iron and diamond in protection as specified, and is deliberately generic-looking (not
 * "batman-only" internally) so future heroes can define their own the same way.
 */
public final class BatSuitArmorMaterial {

    public static final Holder<ArmorMaterial> BAT_SUIT = Holder.direct(new ArmorMaterial(
            18,
            java.util.Map.of(
                    ArmorItem.Type.HELMET, 3,
                    ArmorItem.Type.CHESTPLATE, 7,
                    ArmorItem.Type.LEGGINGS, 6,
                    ArmorItem.Type.BOOTS, 3
            ),
            12,
            SoundEvents.ARMOR_EQUIP_IRON,
            1.5F,
            0.05F,
            // Lazily supplied so referencing HJItems here (which itself references this class) is safe.
            () -> Ingredient.of(com.heroesjourney.item.HJItems.KEVLAR_FIBER.get()),
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, "bat_suit")))
    ));

    private BatSuitArmorMaterial() {
    }
}
