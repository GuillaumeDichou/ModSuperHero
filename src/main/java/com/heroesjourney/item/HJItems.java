package com.heroesjourney.item;

import com.heroesjourney.HeroesJourney;
import com.heroesjourney.entity.boss.BossSpawnerBlock;
import com.heroesjourney.item.armor.BatSuitArmorMaterial;
import com.heroesjourney.item.gadget.BatarangItem;
import com.heroesjourney.item.gadget.GrappleHookItem;
import com.heroesjourney.item.gadget.SmokePebbleItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HJItems {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HeroesJourney.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HeroesJourney.MODID);

    // -----------------------------------------------------------------
    // Blocks
    // -----------------------------------------------------------------

    public static final DeferredBlock<Block> WAYNE_GRAVE_THOMAS = BLOCKS.registerBlock("wayne_grave_thomas",
            props -> new HeroGraveBlock(props), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).sound(SoundType.STONE).strength(-1.0F, 3600000.0F).noOcclusion());

    public static final DeferredBlock<Block> WAYNE_GRAVE_MARTHA = BLOCKS.registerBlock("wayne_grave_martha",
            props -> new HeroGraveBlock(props), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).sound(SoundType.STONE).strength(-1.0F, 3600000.0F).noOcclusion());

    public static final DeferredBlock<Block> BOSS_SPAWNER_BLOCK = BLOCKS.registerBlock("boss_spawner",
            props -> new BossSpawnerBlock(props), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.FIRE).sound(SoundType.STONE).strength(-1.0F, 3600000.0F).noOcclusion().lightLevel(state -> 3));

    public static final DeferredItem<net.minecraft.world.item.BlockItem> WAYNE_GRAVE_THOMAS_ITEM =
            ITEMS.registerSimpleBlockItem("wayne_grave_thomas", WAYNE_GRAVE_THOMAS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> WAYNE_GRAVE_MARTHA_ITEM =
            ITEMS.registerSimpleBlockItem("wayne_grave_martha", WAYNE_GRAVE_MARTHA);

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

    // Durability is derived by ArmorItem itself from the material + slot, so Properties here
    // deliberately doesn't set it (doing so twice throws at registration time).
    public static final DeferredItem<ArmorItem> BAT_COWL = ITEMS.registerItem("bat_cowl",
            props -> new ArmorItem(BatSuitArmorMaterial.BAT_SUIT, ArmorItem.Type.HELMET, props),
            new Item.Properties());

    public static final DeferredItem<ArmorItem> BAT_ARMORED_CHESTPLATE = ITEMS.registerItem("bat_armored_chestplate",
            props -> new ArmorItem(BatSuitArmorMaterial.BAT_SUIT, ArmorItem.Type.CHESTPLATE, props),
            new Item.Properties().rarity(Rarity.RARE));

    public static final DeferredItem<ArmorItem> BAT_LEGGINGS = ITEMS.registerItem("bat_leggings",
            props -> new ArmorItem(BatSuitArmorMaterial.BAT_SUIT, ArmorItem.Type.LEGGINGS, props),
            new Item.Properties());

    public static final DeferredItem<ArmorItem> BAT_BOOTS = ITEMS.registerItem("bat_boots",
            props -> new ArmorItem(BatSuitArmorMaterial.BAT_SUIT, ArmorItem.Type.BOOTS, props),
            new Item.Properties());

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

    public static final DeferredItem<Item> RUSTY_CELL_KEY = ITEMS.registerSimpleItem("rusty_cell_key",
            new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> TREASURE_MAP = ITEMS.registerSimpleItem("treasure_map",
            new Item.Properties().stacksTo(1));

    private HJItems() {
    }
}
