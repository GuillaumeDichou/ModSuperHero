package com.heroesjourney.client;

import com.heroesjourney.client.gui.HudTrackerOverlay;
import com.heroesjourney.client.gui.RosterScreen;
import com.heroesjourney.client.renderer.SimpleHumanoidRenderer;
import com.heroesjourney.entity.HJEntities;
import com.heroesjourney.entity.boss.FearToxinProjectile;
import com.heroesjourney.entity.npc.QuestNpcEntity;
import com.heroesjourney.item.gadget.BatarangEntity;
import com.heroesjourney.item.gadget.SmokePebbleEntity;
import com.heroesjourney.network.HJNetworking;
import com.heroesjourney.network.UseAbilityPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Client-only bootstrap: keybindings, renderers, HUD layer, and the keybind -> screen/packet glue. */
public final class ClientSetup {

    private ClientSetup() {
    }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ClientSetup::registerKeyMappings);
        modEventBus.addListener(ClientSetup::registerRenderers);
        modEventBus.addListener(ClientSetup::registerGuiLayers);
        NeoForge.EVENT_BUS.addListener(ClientSetup::onClientTick);
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.OPEN_ROSTER);
        event.register(KeyBindings.USE_ABILITY);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(HJEntities.QUEST_NPC.get(), ctx -> new SimpleHumanoidRenderer<>(ctx, QuestNpcEntity::npcTexture, 0.5F));
        event.registerEntityRenderer(HJEntities.NINJA.get(), ctx -> new SimpleHumanoidRenderer<>(ctx, e -> ninjaTexture(), 0.5F));
        event.registerEntityRenderer(HJEntities.PRISON_GUARD.get(), ctx -> new SimpleHumanoidRenderer<>(ctx, e -> guardTexture(), 0.5F));
        event.registerEntityRenderer(HJEntities.KEN_BOSS.get(), ctx -> new SimpleHumanoidRenderer<>(ctx, e -> kenTexture(), 0.6F));
        event.registerEntityRenderer(HJEntities.SCARECROW_BOSS.get(), ctx -> new SimpleHumanoidRenderer<>(ctx, e -> scarecrowTexture(), 0.6F));
        event.registerEntityRenderer(HJEntities.HENRI_BOSS.get(), ctx -> new SimpleHumanoidRenderer<>(ctx, e -> henriTexture(), 0.6F));
        event.registerEntityRenderer(HJEntities.BATARANG.get(), ctx -> new ThrownItemRenderer<BatarangEntity>(ctx));
        event.registerEntityRenderer(HJEntities.SMOKE_PEBBLE.get(), ctx -> new ThrownItemRenderer<SmokePebbleEntity>(ctx));
        event.registerEntityRenderer(HJEntities.FEAR_TOXIN.get(), ctx -> new ThrownItemRenderer<FearToxinProjectile>(ctx));
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(com.heroesjourney.HeroesJourney.MODID, "hud_tracker"), new HudTrackerOverlay());
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        while (KeyBindings.OPEN_ROSTER.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new RosterScreen());
            }
        }
        while (KeyBindings.USE_ABILITY.consumeClick()) {
            HJNetworking.sendToServer(new UseAbilityPayload(com.heroesjourney.content.batman.BatmanAbilities.DETECTIVE_SENSE));
        }
    }

    private static ResourceLocation ninjaTexture() {
        return ResourceLocation.fromNamespaceAndPath(com.heroesjourney.HeroesJourney.MODID, "textures/entity/ninja.png");
    }

    private static ResourceLocation kenTexture() {
        return ResourceLocation.fromNamespaceAndPath(com.heroesjourney.HeroesJourney.MODID, "textures/entity/ken.png");
    }

    private static ResourceLocation guardTexture() {
        return ResourceLocation.fromNamespaceAndPath(com.heroesjourney.HeroesJourney.MODID, "textures/entity/prison_guard.png");
    }

    private static ResourceLocation scarecrowTexture() {
        return ResourceLocation.fromNamespaceAndPath(com.heroesjourney.HeroesJourney.MODID, "textures/entity/scarecrow.png");
    }

    private static ResourceLocation henriTexture() {
        return ResourceLocation.fromNamespaceAndPath(com.heroesjourney.HeroesJourney.MODID, "textures/entity/henri.png");
    }
}
