package com.heroesjourney.network;

import com.heroesjourney.HeroesJourney;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: activate a keybind-driven hero ability (e.g. "detective_sense"). */
public record UseAbilityPayload(String abilityId) implements CustomPacketPayload {

    public static final Type<UseAbilityPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, "use_ability"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UseAbilityPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, UseAbilityPayload::abilityId,
            UseAbilityPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
