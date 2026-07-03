package com.heroesjourney.entity.npc;

import com.heroesjourney.dialogue.DialogueRegistry;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Generic quest-giver NPC: invulnerable, never despawns, and defers everything about who it is
 * and what it says to a {@link com.heroesjourney.dialogue.NpcDefinition} looked up by
 * {@link #getNpcId()}. Every hero's NPCs (Ken, Henri, prisoners, ...) reuse this single class -
 * only the id and the associated dialogue tree differ.
 */
public class QuestNpcEntity extends PathfinderMob {

    private static final EntityDataAccessor<String> NPC_ID =
            SynchedEntityData.defineId(QuestNpcEntity.class, EntityDataSerializers.STRING);

    public QuestNpcEntity(EntityType<? extends QuestNpcEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(NPC_ID, "");
    }

    public String getNpcId() {
        return this.entityData.get(NPC_ID);
    }

    public void setNpcId(String id) {
        this.entityData.set(NPC_ID, id);
        DialogueRegistry.npc(id).ifPresent(def -> this.setCustomName(def.displayName()));
        this.setCustomNameVisible(true);
    }

    private static final net.minecraft.resources.ResourceLocation FALLBACK_TEXTURE =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(com.heroesjourney.HeroesJourney.MODID, "textures/entity/npc_fallback.png");

    public net.minecraft.resources.ResourceLocation npcTexture() {
        return DialogueRegistry.npc(getNpcId()).map(com.heroesjourney.dialogue.NpcDefinition::texture).orElse(FALLBACK_TEXTURE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Quest NPCs are always invulnerable; overriding hurt() directly is more version-stable
        // than isInvulnerableTo(), whose signature has changed across recent Minecraft releases.
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceSquared) {
        return false;
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
