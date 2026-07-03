package com.heroesjourney.entity.boss;

import com.heroesjourney.config.HJConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Boss of quest 6 ("La peur elle-même"): keeps its distance and throws fear toxin vials; at half
 * health it releases a one-shot toxin cloud that hits every player in the arena.
 */
public class ScarecrowBoss extends HeroBossEntity implements RangedAttackMob {

    private boolean hasReleasedCloud;

    public ScarecrowBoss(EntityType<? extends ScarecrowBoss> type, Level level) {
        super(type, level, BossEvent.BossBarColor.GREEN);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, HJConfig.SCARECROW_HEALTH.get())
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    public String bossId() {
        return "scarecrow";
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.0D, 40, 14.0F));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!(this.level() instanceof ServerLevel)) {
            return;
        }
        FearToxinProjectile projectile = new FearToxinProjectile(this.level(), this);
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.5) - projectile.getY();
        double dz = target.getZ() - this.getZ();
        projectile.shoot(dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.2, dz, 1.4F, 6.0F);
        this.level().addFreshEntity(projectile);
        this.playSound(net.minecraft.sounds.SoundEvents.WITCH_THROW, 1.0F, 1.0F);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!hasReleasedCloud && this.getHealth() <= this.getMaxHealth() * 0.5F) {
            hasReleasedCloud = true;
            releaseToxinCloud(serverLevel);
        }
    }

    private void releaseToxinCloud(ServerLevel level) {
        double radius = HJConfig.SMOKE_PEBBLE_RADIUS.get() * 1.5;
        level.sendParticles(ParticleTypes.WITCH, this.getX(), this.getY() + 1, this.getZ(), 80, radius * 0.5, 1.0, radius * 0.5, 0.02);
        for (Player player : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(radius))) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 140, 0));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
        }
        this.playSound(net.minecraft.sounds.SoundEvents.WITCH_AMBIENT, 1.5F, 0.6F);
    }
}
