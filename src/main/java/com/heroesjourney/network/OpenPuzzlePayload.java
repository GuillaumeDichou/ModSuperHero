package com.heroesjourney.network;

import com.heroesjourney.HeroesJourney;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server -> client: open the riddle book's puzzle screen with server-generated content for one
 * of the three mini-game types. {@code data} packs the type-specific fields as a single string
 * (joined with {@link #SEP}) rather than a list, to avoid a second StreamCodec shape.
 */
public record OpenPuzzlePayload(int puzzleType, String data) implements CustomPacketPayload {

    /** Field separator used inside {@link #data()}; chosen so it never collides with real content. */
    public static final String SEP = "@@";

    public static final int TYPE_MEMORY = 0;
    public static final int TYPE_LOGIC = 1;
    public static final int TYPE_CIPHER = 2;

    public static final Type<OpenPuzzlePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, "open_puzzle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenPuzzlePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OpenPuzzlePayload::puzzleType,
            ByteBufCodecs.STRING_UTF8, OpenPuzzlePayload::data,
            OpenPuzzlePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
