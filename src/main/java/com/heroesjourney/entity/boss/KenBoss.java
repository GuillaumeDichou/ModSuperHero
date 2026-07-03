package com.heroesjourney.entity.boss;

import com.heroesjourney.config.HJConfig;
import com.heroesjourney.entity.HJEntities;
import com.heroesjourney.entity.mob.NinjaMob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Boss of quest 4 ("Le refus"): fast melee duelist who occasionally teleports behind the player
 * and calls in ninja reinforcements once he drops below half health.
 */
public class KenBoss extends HeroBossEntity {

    private static final int TELEPORT_COOLDOWN = 100;
    private int teleportTimer;
    private boolean hasSummoned;

    public KenBoss(EntityType<? extends KenBoss> type, Level level) {
        super(type, level, BossEvent.BossBarColor.RED);
        applyConfiguredHealth(HJConfig.KEN_HEALTH.get());
    }

    // Default here (150) matches HJConfig's default and is only used as the attribute's
    // registration-time placeholder; the constructor overwrites it with the real config value
    // once configs are actually loaded (see HeroBossEntity#applyConfiguredHealth).
    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 150.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.ATTACK_SPEED, 0.2D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    @Override
    public String bossId() {
        return "ken";
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        LivingEntity target = this.getTarget();
        if (teleportTimer > 0) {
            teleportTimer--;
        }
        if (target != null && teleportTimer <= 0 && this.distanceTo(target) > 6.0D) {
            teleportBehind(target);
            teleportTimer = TELEPORT_COOLDOWN;
        }
        if (!hasSummoned && this.getHealth() <= this.getMaxHealth() * 0.5F) {
            hasSummoned = true;
            summonMinions(serverLevel);
        }
    }

    private void teleportBehind(LivingEntity target) {
        double angle = Math.toRadians(target.getYRot() + 180);
        double x = target.getX() + Math.sin(angle) * 2.5D;
        double z = target.getZ() - Math.cos(angle) * 2.5D;
        this.randomTeleport(x, target.getY(), z, false);
    }

    private void summonMinions(ServerLevel level) {
        for (int i = 0; i < 2; i++) {
            NinjaMob minion = HJEntities.NINJA.get().create(level);
            if (minion == null) {
                continue;
            }
            double angle = i * Math.PI;
            minion.moveTo(this.getX() + Math.cos(angle) * 2.0D, this.getY(), this.getZ() + Math.sin(angle) * 2.0D, this.getYRot(), 0);
            minion.setHealth(minion.getMaxHealth() * 0.5F);
            level.addFreshEntity(minion);
            if (this.getTarget() != null) {
                minion.setTarget(this.getTarget());
            }
        }
    }
}
