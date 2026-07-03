package com.heroesjourney.entity;

import com.heroesjourney.HeroesJourney;
import com.heroesjourney.entity.boss.BossSpawnerBlockEntity;
import com.heroesjourney.entity.boss.FearToxinProjectile;
import com.heroesjourney.entity.boss.HenriBoss;
import com.heroesjourney.entity.boss.KenBoss;
import com.heroesjourney.entity.boss.ScarecrowBoss;
import com.heroesjourney.entity.mob.NinjaMob;
import com.heroesjourney.entity.mob.PrisonGuardMob;
import com.heroesjourney.entity.npc.QuestNpcEntity;
import com.heroesjourney.item.HJItems;
import com.heroesjourney.item.gadget.BatarangEntity;
import com.heroesjourney.item.gadget.SmokePebbleEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
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
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HeroesJourney.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<QuestNpcEntity>> QUEST_NPC = ENTITY_TYPES.register("quest_npc",
            () -> EntityType.Builder.of(QuestNpcEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build("quest_npc"));

    public static final DeferredHolder<EntityType<?>, EntityType<NinjaMob>> NINJA = ENTITY_TYPES.register("ninja",
            () -> EntityType.Builder.of(NinjaMob::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.85F)
                    .clientTrackingRange(8)
                    .build("ninja"));

    public static final DeferredHolder<EntityType<?>, EntityType<PrisonGuardMob>> PRISON_GUARD = ENTITY_TYPES.register("prison_guard",
            () -> EntityType.Builder.of(PrisonGuardMob::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
                    .build("prison_guard"));

    public static final DeferredHolder<EntityType<?>, EntityType<KenBoss>> KEN_BOSS = ENTITY_TYPES.register("ken_boss",
            () -> EntityType.Builder.of(KenBoss::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(16)
                    .fireImmune()
                    .build("ken_boss"));

    public static final DeferredHolder<EntityType<?>, EntityType<ScarecrowBoss>> SCARECROW_BOSS = ENTITY_TYPES.register("scarecrow_boss",
            () -> EntityType.Builder.of(ScarecrowBoss::new, MobCategory.MONSTER)
                    .sized(0.7F, 2.2F)
                    .clientTrackingRange(16)
                    .fireImmune()
                    .build("scarecrow_boss"));

    public static final DeferredHolder<EntityType<?>, EntityType<HenriBoss>> HENRI_BOSS = ENTITY_TYPES.register("henri_boss",
            () -> EntityType.Builder.of(HenriBoss::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(16)
                    .fireImmune()
                    .build("henri_boss"));

    public static final DeferredHolder<EntityType<?>, EntityType<FearToxinProjectile>> FEAR_TOXIN = ENTITY_TYPES.register("fear_toxin",
            () -> EntityType.Builder.<FearToxinProjectile>of(FearToxinProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("fear_toxin"));

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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BossSpawnerBlockEntity>> BOSS_SPAWNER_BE = BLOCK_ENTITY_TYPES.register("boss_spawner",
            () -> BlockEntityType.Builder.of(BossSpawnerBlockEntity::new, HJItems.BOSS_SPAWNER_BLOCK.get()).build(null));

    /** Registered from the mod constructor via {@code modEventBus.addListener(HJEntities::registerAttributes)}. */
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(QUEST_NPC.get(), QuestNpcEntity.createAttributes().build());
        event.put(NINJA.get(), NinjaMob.createAttributes().build());
        event.put(PRISON_GUARD.get(), PrisonGuardMob.createAttributes().build());
        event.put(KEN_BOSS.get(), KenBoss.createAttributes().build());
        event.put(SCARECROW_BOSS.get(), ScarecrowBoss.createAttributes().build());
        event.put(HENRI_BOSS.get(), HenriBoss.createAttributes().build());
    }

    private HJEntities() {
    }
}
