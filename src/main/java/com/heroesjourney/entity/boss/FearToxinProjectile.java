package com.heroesjourney.entity.boss;

import com.heroesjourney.entity.HJEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Scarecrow's ranged attack: a thrown toxin vial that briefly nauseates and blinds whoever it hits. */
public class FearToxinProjectile extends ThrowableItemProjectile {

    public FearToxinProjectile(EntityType<? extends FearToxinProjectile> type, Level level) {
        super(type, level);
    }

    public FearToxinProjectile(Level level, LivingEntity shooter) {
        super(HJEntities.FEAR_TOXIN.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SPLASH_POTION;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity living && !this.level().isClientSide) {
            living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.WITCH, this.getX(), this.getY(), this.getZ(), 20, 0.4, 0.4, 0.4, 0.01);
            this.discard();
        }
    }
}
