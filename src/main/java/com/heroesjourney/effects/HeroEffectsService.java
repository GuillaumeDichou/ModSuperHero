package com.heroesjourney.effects;

import com.heroesjourney.data.HJAttachments;
import com.heroesjourney.data.HeroData;
import com.heroesjourney.data.HeroProgress;
import com.heroesjourney.hero.HeroDefinition;
import com.heroesjourney.hero.HeroRegistry;
import net.minecraft.server.level.ServerPlayer;

/**
 * Generic glue between the quest engine and each hero's {@link com.heroesjourney.hero.HeroEffectsProvider}.
 * Contains no hero-specific logic itself.
 */
public final class HeroEffectsService {

    private static final int TICK_INTERVAL = 20;

    private HeroEffectsService() {
    }

    public static void applyImmediate(ServerPlayer player, String heroId, HeroProgress progress) {
        HeroRegistry.get(heroId).ifPresent(hero -> hero.effects().applyImmediate(player, progress));
    }

    public static void clearAll(ServerPlayer player) {
        HeroData data = player.getData(HJAttachments.HERO_DATA);
        if (!data.hasActiveHero()) {
            return;
        }
        HeroRegistry.get(data.activeHero()).ifPresent(hero -> hero.effects().clear(player));
    }

    public static void tick(ServerPlayer player) {
        if (player.tickCount % TICK_INTERVAL != 0) {
            return;
        }
        HeroData data = player.getData(HJAttachments.HERO_DATA);
        if (!data.hasActiveHero()) {
            return;
        }
        HeroDefinition hero = HeroRegistry.get(data.activeHero()).orElse(null);
        if (hero == null) {
            return;
        }
        HeroProgress progress = data.getOrCreateProgress(hero.id());
        hero.effects().tick(player, progress);
    }
}
