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
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BossSpawnerBlockEntity>> BOSS_SPAWNER_BE = BLOCK_ENTITY_TYPES.register("boss_spawner",
            () -> BlockEntityType.Builder.of(BossSpawnerBlockEntity::new, HJItems.BOSS_SPAWNER_BLOCK.get()).build(null));

    private HJEntities() {
    }
}
