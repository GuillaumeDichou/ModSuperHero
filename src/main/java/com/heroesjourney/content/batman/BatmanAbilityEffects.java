package com.heroesjourney.content.batman;

import com.heroesjourney.config.HJConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;

/**
 * Quest-4 reward ability: unlike the old "reveal chests" idea, vanilla's Glowing effect only
 * ever applies to entities, never to blocks - so it genuinely works here, applied to every
 * hostile mob within range. This gives the real "silhouette visible through walls" render
 * vanilla already ships (the same one spectral arrows use), no custom outline rendering needed.
 */
public final class BatmanAbilityEffects {

    private BatmanAbilityEffects() {
    }

    public static void threatGlow(ServerPlayer player) {
        int radius = HJConfig.THREAT_GLOW_RADIUS.get();
        int durationTicks = HJConfig.THREAT_GLOW_DURATION_TICKS.get();
        var box = player.getBoundingBox().inflate(radius);
        int glowed = 0;
        for (Mob mob : player.level().getEntitiesOfClass(Mob.class, box)) {
            if (mob instanceof Enemy && mob.isAlive() && mob.closerThan(player, radius)) {
                mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, durationTicks, 0, false, true, true));
                glowed++;
            }
        }
        player.displayClientMessage(Component.translatable("heroesjourney.ability.threat_glow.used", glowed), true);
    }
}
