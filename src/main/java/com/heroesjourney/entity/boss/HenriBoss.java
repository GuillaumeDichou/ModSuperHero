package com.heroesjourney.entity.boss;

import com.heroesjourney.config.HJConfig;
import com.heroesjourney.entity.HJEntities;
import com.heroesjourney.entity.mob.NinjaMob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
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
 * Final boss of the Batman Begins arc ("Le train de la peur"): Henri reveals himself as the true
 * Ra's al Ghul. Cycles between a brief parry stance (reduced damage taken) and an opening window
 * (bonus damage taken) so players learn to time their hits; arrives with 2-3 League ninjas.
 */
public class HenriBoss extends HeroBossEntity {

    private static final int CYCLE_LENGTH = 200;
    private static final int PARRY_WINDOW = 40;
    private static final int VULNERABLE_WINDOW = 50;

    private int combatTimer;
    private boolean summonedEscort;

    public HenriBoss(EntityType<? extends HenriBoss> type, Level level) {
        super(type, level, BossEvent.BossBarColor.PURPLE);
        applyConfiguredHealth(HJConfig.HENRI_HEALTH.get());
    }

    // Default here (250) matches HJConfig's default; see HeroBossEntity#applyConfiguredHealth.
    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 250.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ATTACK_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 6.0D);
    }

    @Override
    public String bossId() {
        return "henri";
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    private boolean isParrying() {
        int phase = combatTimer % CYCLE_LENGTH;
        return phase < PARRY_WINDOW;
    }

    private boolean isVulnerable() {
        int phase = combatTimer % CYCLE_LENGTH;
        return phase >= PARRY_WINDOW && phase < PARRY_WINDOW + VULNERABLE_WINDOW;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        float scaled = amount;
        if (isParrying()) {
            scaled *= 0.15F;
        } else if (isVulnerable()) {
            scaled *= 1.75F;
        }
        return super.hurt(source, scaled);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (this.getTarget() != null) {
            combatTimer++;
        }
        if (!summonedEscort && this.tickCount > 5) {
            summonedEscort = true;
            summonEscort(serverLevel);
        }
    }

    private void summonEscort(ServerLevel level) {
        int count = 2 + this.random.nextInt(2);
        for (int i = 0; i < count; i++) {
            NinjaMob ninja = HJEntities.NINJA.get().create(level);
            if (ninja == null) {
                continue;
            }
            double angle = (Math.PI * 2 / count) * i;
            ninja.moveTo(this.getX() + Math.cos(angle) * 3.0D, this.getY(), this.getZ() + Math.sin(angle) * 3.0D, this.getYRot(), 0);
            level.addFreshEntity(ninja);
        }
    }
}
