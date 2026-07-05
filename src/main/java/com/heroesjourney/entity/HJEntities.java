package com.heroesjourney.entity;

import com.heroesjourney.HeroesJourney;
import com.heroesjourney.item.gadget.BatarangEntity;
import com.heroesjourney.item.gadget.SmokePebbleEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Every entity type the mod registers, in one class. Thrown-gadget entity types used to live in
 * a sibling {@code HJItemEntities} class "to keep files small" - that was a bug: a class that's
 * never otherwise referenced isn't initialized by the JVM until something touches it, so those
 * registrations ran (too late, after the registry event had already fired) only when
 * {@code ClientSetup} first referenced {@code HJItemEntities.BATARANG}. Keeping every
 * {@code DeferredRegister.register(...)} call in the same class that the mod constructor touches
 * during the mod-bus registration phase guarantees they all run together, on time.
 */
public final class HJEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, HeroesJourney.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<BatarangEntity>> BATARANG = ENTITY_TYPES.register("batarang",
            () -> EntityType.Builder.<BatarangEntity>of(BatarangEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.15F)
                    .clientTrackingRange(6)
                    .updateInterval(2)
                    .build("batarang"));

    public static final DeferredHolder<EntityType<?>, EntityType<SmokePebbleEntity>> SMOKE_PEBBLE = ENTITY_TYPES.register("smoke_pebble",
            () -> EntityType.Builder.<SmokePebbleEntity>of(SmokePebbleEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(6)
                    .updateInterval(4)
                    .build("smoke_pebble"));

    private HJEntities() {
    }
}
