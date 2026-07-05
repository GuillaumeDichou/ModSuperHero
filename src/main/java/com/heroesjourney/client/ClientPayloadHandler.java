package com.heroesjourney.client;

import com.heroesjourney.client.gui.PuzzleScreen;
import com.heroesjourney.data.HeroData;
import com.heroesjourney.network.OpenPuzzlePayload;
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

    public static void handleOpenPuzzle(OpenPuzzlePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> Minecraft.getInstance().setScreen(new PuzzleScreen(payload)));
    }
}
