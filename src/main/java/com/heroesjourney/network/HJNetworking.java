package com.heroesjourney.network;

import com.heroesjourney.HeroesJourney;
import com.heroesjourney.client.ClientPayloadHandler;
import com.heroesjourney.data.HJAttachments;
import com.heroesjourney.data.HeroData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.PacketDistributor;

public final class HJNetworking {

    private HJNetworking() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(ActivateHeroPayload.TYPE, ActivateHeroPayload.STREAM_CODEC, ServerPayloadHandler::handleActivateHero);
        registrar.playToServer(SetHudTrackerPayload.TYPE, SetHudTrackerPayload.STREAM_CODEC, ServerPayloadHandler::handleSetHudTracker);
        registrar.playToServer(DialogueChoicePayload.TYPE, DialogueChoicePayload.STREAM_CODEC, ServerPayloadHandler::handleDialogueChoice);
        registrar.playToServer(UseAbilityPayload.TYPE, UseAbilityPayload.STREAM_CODEC, ServerPayloadHandler::handleUseAbility);

        if (FMLEnvironment.dist.isClient()) {
            registrar.playToClient(SyncHeroDataPayload.TYPE, SyncHeroDataPayload.STREAM_CODEC, ClientPayloadHandler::handleSyncHeroData);
            registrar.playToClient(OpenDialoguePayload.TYPE, OpenDialoguePayload.STREAM_CODEC, ClientPayloadHandler::handleOpenDialogue);
        }
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    public static void syncHeroData(ServerPlayer player) {
        HeroData data = player.getData(HJAttachments.HERO_DATA);
        CompoundTag tag = (CompoundTag) HeroData.CODEC.encodeStart(NbtOps.INSTANCE, data)
                .result().orElseThrow(() -> new IllegalStateException("Failed to encode HeroData"));
        sendToPlayer(player, new SyncHeroDataPayload(tag));
    }
}
