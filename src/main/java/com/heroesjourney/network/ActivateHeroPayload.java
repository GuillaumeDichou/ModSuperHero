package com.heroesjourney.network;

import com.heroesjourney.HeroesJourney;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: "please make this hero active" (sent after the roster confirmation prompt). */
public record ActivateHeroPayload(String heroId) implements CustomPacketPayload {

    public static final Type<ActivateHeroPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, "activate_hero"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ActivateHeroPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ActivateHeroPayload::heroId,
            ActivateHeroPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
