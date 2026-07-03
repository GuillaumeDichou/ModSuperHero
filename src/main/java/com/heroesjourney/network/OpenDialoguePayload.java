package com.heroesjourney.network;

import com.heroesjourney.HeroesJourney;
import com.heroesjourney.dialogue.DialogueView;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server -> client: open (or update) the dialogue screen with a resolved {@link DialogueView}. */
public record OpenDialoguePayload(CompoundTag view) implements CustomPacketPayload {

    public static final Type<OpenDialoguePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, "open_dialogue"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenDialoguePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.TRUSTED_COMPOUND_TAG, OpenDialoguePayload::view,
            OpenDialoguePayload::new
    );

    public static OpenDialoguePayload of(DialogueView view) {
        CompoundTag tag = (CompoundTag) DialogueView.CODEC.encodeStart(NbtOps.INSTANCE, view)
                .result().orElseThrow(() -> new IllegalStateException("Failed to encode DialogueView"));
        return new OpenDialoguePayload(tag);
    }

    public DialogueView toView() {
        return DialogueView.CODEC.parse(NbtOps.INSTANCE, view)
                .result().orElseThrow(() -> new IllegalStateException("Failed to decode DialogueView"));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
