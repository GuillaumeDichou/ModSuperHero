package com.heroesjourney.network;

import com.heroesjourney.HeroesJourney;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server -> client: the local player's full {@code HeroData}, NBT-encoded via {@code HeroData.CODEC}. */
public record SyncHeroDataPayload(CompoundTag data) implements CustomPacketPayload {

    public static final Type<SyncHeroDataPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, "sync_hero_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncHeroDataPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.TRUSTED_COMPOUND_TAG, SyncHeroDataPayload::data,
            SyncHeroDataPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
