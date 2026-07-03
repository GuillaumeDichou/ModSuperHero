package com.heroesjourney.entity;

import com.heroesjourney.HeroesJourney;
import com.heroesjourney.item.gadget.BatarangEntity;
import com.heroesjourney.item.gadget.SmokePebbleEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Entity types for thrown gadgets. Split from {@link HJEntities} only to keep files small. */
public final class HJItemEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = HJEntities.ENTITY_TYPES;

    private static ResourceKey<EntityType<?>> key(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, name));
    }

    public static final DeferredHolder<EntityType<?>, EntityType<BatarangEntity>> BATARANG = ENTITY_TYPES.register("batarang",
            () -> EntityType.Builder.<BatarangEntity>of(BatarangEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.15F)
                    .clientTrackingRange(6)
                    .updateInterval(2)
                    .build(key("batarang")));

    public static final DeferredHolder<EntityType<?>, EntityType<SmokePebbleEntity>> SMOKE_PEBBLE = ENTITY_TYPES.register("smoke_pebble",
            () -> EntityType.Builder.<SmokePebbleEntity>of(SmokePebbleEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(6)
                    .updateInterval(4)
                    .build(key("smoke_pebble")));

    private HJItemEntities() {
    }
}
