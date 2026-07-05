package com.heroesjourney.item;

import com.heroesjourney.HeroesJourney;
import com.heroesjourney.item.armor.BatSuitArmorMaterial;
import com.heroesjourney.item.gadget.BatarangItem;
import com.heroesjourney.item.gadget.GrappleHookItem;
import com.heroesjourney.item.gadget.SmokePebbleItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HJItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HeroesJourney.MODID);

    // -----------------------------------------------------------------
    // Materials
    // -----------------------------------------------------------------

    public static final DeferredItem<Item> KEVLAR_FIBER = ITEMS.registerSimpleItem("kevlar_fiber");
    public static final DeferredItem<Item> MEMORY_CLOTH = ITEMS.registerSimpleItem("memory_cloth");
    public static final DeferredItem<Item> ELECTRONIC_COMPONENT = ITEMS.registerSimpleItem("electronic_component");

    // -----------------------------------------------------------------
    // Armor - intermediate crafting pieces
    // -----------------------------------------------------------------

    public static final DeferredItem<Item> BAT_COMBAT_VEST = ITEMS.registerSimpleItem("bat_combat_vest");
    public static final DeferredItem<Item> BAT_CAPE = ITEMS.registerSimpleItem("bat_cape");

    // -----------------------------------------------------------------
    // Armor - wearable pieces
    // -----------------------------------------------------------------

    // ArmorMaterial no longer carries a durability multiplier (1.21.1), so each piece sets its
    // own durability here via ArmorItem.Type#getDurability(int).
    public static final DeferredItem<ArmorItem> BAT_COWL = ITEMS.registerItem("bat_cowl",
            props -> new ArmorItem(BatSuitArmorMaterial.BAT_SUIT, ArmorItem.Type.HELMET, props),
            new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(BatSuitArmorMaterial.BASE_DURABILITY)));

    public static final DeferredItem<ArmorItem> BAT_ARMORED_CHESTPLATE = ITEMS.registerItem("bat_armored_chestplate",
            props -> new ArmorItem(BatSuitArmorMaterial.BAT_SUIT, ArmorItem.Type.CHESTPLATE, props),
            new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(BatSuitArmorMaterial.BASE_DURABILITY)).rarity(Rarity.RARE));

    public static final DeferredItem<ArmorItem> BAT_LEGGINGS = ITEMS.registerItem("bat_leggings",
            props -> new ArmorItem(BatSuitArmorMaterial.BAT_SUIT, ArmorItem.Type.LEGGINGS, props),
            new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(BatSuitArmorMaterial.BASE_DURABILITY)));

    public static final DeferredItem<ArmorItem> BAT_BOOTS = ITEMS.registerItem("bat_boots",
            props -> new ArmorItem(BatSuitArmorMaterial.BAT_SUIT, ArmorItem.Type.BOOTS, props),
            new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(BatSuitArmorMaterial.BASE_DURABILITY)));

    // -----------------------------------------------------------------
    // Gadgets
    // -----------------------------------------------------------------

    public static final DeferredItem<BatarangItem> BATARANG = ITEMS.registerItem("batarang",
            BatarangItem::new, new Item.Properties().stacksTo(16));

    public static final DeferredItem<GrappleHookItem> GRAPPLE_HOOK = ITEMS.registerItem("grapple_hook",
            GrappleHookItem::new, new Item.Properties().stacksTo(1));

    public static final DeferredItem<SmokePebbleItem> SMOKE_PEBBLE = ITEMS.registerItem("smoke_pebble",
            SmokePebbleItem::new, new Item.Properties().stacksTo(8));

    // -----------------------------------------------------------------
    // Quest items
    // -----------------------------------------------------------------

    public static final DeferredItem<RiddleBookItem> RIDDLE_BOOK = ITEMS.registerItem("riddle_book",
            RiddleBookItem::new, new Item.Properties().stacksTo(1));

    private HJItems() {
    }
}
