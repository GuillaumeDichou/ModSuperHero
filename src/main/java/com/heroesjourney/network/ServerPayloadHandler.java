package com.heroesjourney.network;

import com.heroesjourney.quest.QuestManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server-side handlers for client -> server payloads. Runs the actual logic on the main thread. */
public final class ServerPayloadHandler {

    private ServerPayloadHandler() {
    }

    public static void handleActivateHero(ActivateHeroPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                QuestManager.INSTANCE.requestActivateHero(player, payload.heroId());
            }
        });
    }

    public static void handleSetHudTracker(SetHudTrackerPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                QuestManager.INSTANCE.setHudTrackerEnabled(player, payload.enabled());
            }
        });
    }

    public static void handleDialogueChoice(DialogueChoicePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                QuestManager.INSTANCE.handleDialogueChoice(player, payload.npcEntityId(), payload.choiceId());
            }
        });
    }

    public static void handleUseAbility(UseAbilityPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                QuestManager.INSTANCE.handleUseAbility(player, payload.abilityId());
            }
        });
    }
}
