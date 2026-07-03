package com.heroesjourney.client;

import com.heroesjourney.client.gui.DialogueScreen;
import com.heroesjourney.data.HeroData;
import com.heroesjourney.dialogue.DialogueView;
import com.heroesjourney.network.OpenDialoguePayload;
import com.heroesjourney.network.SyncHeroDataPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client-side handlers for server -> client payloads. */
public final class ClientPayloadHandler {

    private ClientPayloadHandler() {
    }

    public static void handleSyncHeroData(SyncHeroDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            HeroData data = HeroData.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, payload.data())
                    .result().orElse(new HeroData());
            ClientHeroDataCache.set(data);
        });
    }

    public static void handleOpenDialogue(OpenDialoguePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            DialogueView view = payload.toView();
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof DialogueScreen current) {
                current.updateView(view);
            } else {
                mc.setScreen(new DialogueScreen(view));
            }
        });
    }
}
