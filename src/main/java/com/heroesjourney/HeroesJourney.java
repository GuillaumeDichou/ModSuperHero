package com.heroesjourney;

import com.heroesjourney.client.ClientSetup;
import com.heroesjourney.command.HJCommand;
import com.heroesjourney.config.HJConfig;
import com.heroesjourney.content.batman.BatmanContent;
import com.heroesjourney.data.HJAttachments;
import com.heroesjourney.entity.HJEntities;
import com.heroesjourney.hero.HeroRegistry;
import com.heroesjourney.item.HJCreativeTabs;
import com.heroesjourney.item.HJItems;
import com.heroesjourney.network.HJNetworking;
import com.heroesjourney.quest.QuestManager;
import com.heroesjourney.structure.HJStructures;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

/**
 * Entry point of the "Hero's Journey" mod.
 * <p>
 * This mod is designed to eventually host several hero questlines (Batman, other DC/Marvel
 * heroes, ...). Everything generic (hero registry, quest engine, hero data, GUIs, generic
 * NPC/boss entities) lives in the top level packages; hero-specific content lives under
 * {@code content.<hero>}. The "Batman Begins" arc registered in {@link BatmanContent} is the
 * first and, for now, only piece of content plugged into that generic architecture.
 */
@Mod(HeroesJourney.MODID)
public class HeroesJourney {

    public static final String MODID = "heroesjourney";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HeroesJourney(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, HJConfig.SPEC);

        HJItems.ITEMS.register(modEventBus);
        HJItems.BLOCKS.register(modEventBus);
        HJCreativeTabs.TABS.register(modEventBus);
        HJEntities.ENTITY_TYPES.register(modEventBus);
        HJAttachments.ATTACHMENT_TYPES.register(modEventBus);
        HJStructures.STRUCTURE_TYPES.register(modEventBus);
        com.heroesjourney.structure.HJStructurePieceTypes.STRUCTURE_PIECE_TYPES.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(HJNetworking::register);

        NeoForge.EVENT_BUS.register(QuestManager.INSTANCE);
        NeoForge.EVENT_BUS.register(new com.heroesjourney.item.armor.ArmorEffectsHandler());
        NeoForge.EVENT_BUS.register(new com.heroesjourney.item.gadget.GrappleHandler());
        NeoForge.EVENT_BUS.register(new com.heroesjourney.content.batman.BatmanCombatHandler());
        NeoForge.EVENT_BUS.register(new com.heroesjourney.content.batman.WayneManorProtection());
        NeoForge.EVENT_BUS.register(new com.heroesjourney.content.batman.BatmanRecipeGate());
        NeoForge.EVENT_BUS.addListener(this::registerCommands);

        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            ClientSetup.init(modEventBus);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            HeroRegistry.clear();
            BatmanContent.register();
            LOGGER.info("Hero's Journey: {} hero(es) registered ({})", HeroRegistry.all().size(), HeroRegistry.all());
        });
    }

    private void registerCommands(RegisterCommandsEvent event) {
        HJCommand.register(event.getDispatcher());
    }
}
