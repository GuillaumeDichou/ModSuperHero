package com.heroesjourney.item.gadget;

import com.heroesjourney.config.HJConfig;
import com.heroesjourney.entity.HJEntities;
import com.heroesjourney.item.HJItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** Thrown batarang: deals a light hit + slows its target, then boomerangs back to the thrower. */
public class BatarangEntity extends ThrowableItemProjectile {

    private static final int MAX_FLIGHT_TICKS = 30;
    private boolean returning;

    public BatarangEntity(EntityType<? extends BatarangEntity> type, Level level) {
        super(type, level);
    }

    public BatarangEntity(Level level, LivingEntity owner) {
        super(HJEntities.BATARANG.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return HJItems.BATARANG.get();
    }

    @Override
    public void tick() {
        Entity owner = this.getOwner();
        if (returning && owner != null) {
            Vec3 toOwner = owner.position().add(0, owner.getEyeHeight() * 0.5, 0).subtract(this.position());
            if (toOwner.length() < 1.25D) {
                if (!this.level().isClientSide && owner instanceof net.minecraft.world.entity.player.Player player) {
                    if (!player.getInventory().add(new net.minecraft.world.item.ItemStack(HJItems.BATARANG.get()))) {
                        this.spawnAtLocation(new net.minecraft.world.item.ItemStack(HJItems.BATARANG.get()));
                    }
                }
                this.discard();
                return;
            }
            this.setDeltaMovement(toOwner.normalize().scale(1.4D));
            this.hasImpulse = true;
        } else if (this.tickCount > MAX_FLIGHT_TICKS) {
            returning = true;
        }
        super.tick();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide) {
            return;
        }
        if (result.getEntity() instanceof LivingEntity living && this.getOwner() instanceof LivingEntity owner) {
            living.hurt(this.damageSources().thrown(this, owner), 3.0F);
            int slowDuration = HJConfig.BATARANG_SLOW_DURATION_TICKS.get();
            if (slowDuration > 0) {
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slowDuration, 1));
            }
        }
        returning = true;
    }

    @Override
    protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {
        super.onHitBlock(result);
        returning = true;
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 5, 0.1, 0.1, 0.1, 0.02);
        }
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }
}
