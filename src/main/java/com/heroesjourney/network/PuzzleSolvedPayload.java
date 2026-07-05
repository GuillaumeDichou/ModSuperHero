package com.heroesjourney.network;

import com.heroesjourney.HeroesJourney;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: the riddle book puzzle currently open for this player was solved. */
public record PuzzleSolvedPayload() implements CustomPacketPayload {

    public static final Type<PuzzleSolvedPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, "puzzle_solved"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PuzzleSolvedPayload> STREAM_CODEC =
            StreamCodec.unit(new PuzzleSolvedPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
