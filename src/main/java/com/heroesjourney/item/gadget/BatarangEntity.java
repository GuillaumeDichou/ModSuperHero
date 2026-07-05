package com.heroesjourney.item.gadget;

import com.heroesjourney.config.HJConfig;
import com.heroesjourney.entity.HJEntities;
import com.heroesjourney.item.HJItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Thrown batarang: deals a light hit + slows its target, then behaves like a vanilla arrow - if
 * it doesn't hit anything worth reacting to, it just rests where it landed and can be picked back
 * up by walking over it, rather than boomeranging back to the thrower.
 */
public class BatarangEntity extends ThrowableItemProjectile {

    private static final int MAX_GROUND_LIFETIME_TICKS = 6000;

    private boolean atRest;
    private int groundTicks;

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
        super.tick();
        if (atRest && ++groundTicks > MAX_GROUND_LIFETIME_TICKS) {
            this.discard();
        }
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
        settleOnGround();
    }

    @Override
    protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 5, 0.1, 0.1, 0.1, 0.02);
        }
        settleOnGround();
    }

    private void settleOnGround() {
        this.setDeltaMovement(Vec3.ZERO);
        atRest = true;
    }

    @Override
    public void playerTouch(Player player) {
        if (this.level().isClientSide || !atRest) {
            return;
        }
        if (player.getInventory().add(new ItemStack(HJItems.BATARANG.get()))) {
            this.playSound(SoundEvents.ITEM_PICKUP, 0.2F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            this.discard();
        }
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }
}
