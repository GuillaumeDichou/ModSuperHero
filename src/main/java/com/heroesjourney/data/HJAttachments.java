package com.heroesjourney.data;

import com.heroesjourney.HeroesJourney;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class HJAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, HeroesJourney.MODID);

    /**
     * Per-player attachment holding everything defined in {@link HeroData}. {@code copyOnDeath()}
     * is required here: the player entity instance is recreated on respawn, and without it this
     * data would silently reset to defaults on every death.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HeroData>> HERO_DATA =
            ATTACHMENT_TYPES.register("hero_data", () -> AttachmentType.builder(HeroData::new)
                    .serialize(HeroData.CODEC)
                    .copyOnDeath()
                    .build());

    private HJAttachments() {
    }
}
