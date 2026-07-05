package com.heroesjourney.item.gadget;

import com.heroesjourney.config.HJConfig;
import com.heroesjourney.entity.HJEntities;
import com.heroesjourney.item.HJItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Thrown smoke pebble: on landing, creates a lingering smoke cloud that blinds every entity
 * inside it - mobs and players alike, including whoever threw it - and un-targets nearby
 * hostiles.
 */
public class SmokePebbleEntity extends ThrowableItemProjectile {

    private int cloudTicksRemaining = -1;

    public SmokePebbleEntity(EntityType<? extends SmokePebbleEntity> type, Level level) {
        super(type, level);
    }

    public SmokePebbleEntity(Level level, LivingEntity owner) {
        super(HJEntities.SMOKE_PEBBLE.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return HJItems.SMOKE_PEBBLE.get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (cloudTicksRemaining < 0) {
            cloudTicksRemaining = HJConfig.SMOKE_PEBBLE_DURATION_TICKS.get();
            this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (cloudTicksRemaining < 0) {
            return;
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            double radius = HJConfig.SMOKE_PEBBLE_RADIUS.get();
            if (this.tickCount % 5 == 0) {
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.3, this.getZ(), 6, radius * 0.3, 0.4, radius * 0.3, 0.01);
                for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius))) {
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
                    if (entity instanceof Mob mob) {
                        mob.setTarget(null);
                    }
                }
            }
            cloudTicksRemaining--;
            if (cloudTicksRemaining <= 0) {
                this.discard();
            }
        }
    }

    @Override
    public boolean isNoGravity() {
        return cloudTicksRemaining >= 0;
    }
}
