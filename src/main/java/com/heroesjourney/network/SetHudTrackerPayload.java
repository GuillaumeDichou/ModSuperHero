package com.heroesjourney.network;

import com.heroesjourney.HeroesJourney;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: toggle the optional HUD objective tracker. */
public record SetHudTrackerPayload(boolean enabled) implements CustomPacketPayload {

    public static final Type<SetHudTrackerPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, "set_hud_tracker"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetHudTrackerPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SetHudTrackerPayload::enabled,
            SetHudTrackerPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
