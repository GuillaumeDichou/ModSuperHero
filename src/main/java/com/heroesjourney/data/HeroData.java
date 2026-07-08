package com.heroesjourney.data;

import com.heroesjourney.hero.HeroRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;

/**
 * Per-player persistent state for the whole mod: which hero is active and, for every hero the
 * player has ever touched, their {@link HeroProgress}. Attached to the player via
 * {@link HJAttachments#HERO_DATA}, synced to the owning client whenever it changes
 * (see {@code com.heroesjourney.network}).
 */
public final class HeroData {

    private String activeHero;
    private final Map<String, HeroProgress> perHero;
    private boolean hudTrackerEnabled;

    /** Server-only, not persisted: ability id -> game time (in ticks) it becomes usable again. */
    private final transient Map<String, Long> abilityCooldowns = new HashMap<>();

    public HeroData() {
        this(HeroRegistry.NONE, new HashMap<>(), true);
    }

    public HeroData(String activeHero, Map<String, HeroProgress> perHero, boolean hudTrackerEnabled) {
        this.activeHero = activeHero;
        this.perHero = new HashMap<>(perHero);
        this.hudTrackerEnabled = hudTrackerEnabled;
    }

    public String activeHero() {
        return activeHero;
    }

    public void setActiveHero(String heroId) {
        this.activeHero = heroId;
    }

    public boolean hasActiveHero() {
        return activeHero != null && !activeHero.equals(HeroRegistry.NONE);
    }

    public Map<String, HeroProgress> perHero() {
        return perHero;
    }

    public HeroProgress getOrCreateProgress(String heroId) {
        return perHero.computeIfAbsent(heroId, id -> new HeroProgress());
    }

    public HeroProgress getProgress(String heroId) {
        return perHero.get(heroId);
    }

    public boolean hudTrackerEnabled() {
        return hudTrackerEnabled;
    }

    public void setHudTrackerEnabled(boolean enabled) {
        this.hudTrackerEnabled = enabled;
    }

    public Map<String, Long> abilityCooldowns() {
        return abilityCooldowns;
    }

    public boolean isAbilityReady(String abilityId, long gameTime) {
        return gameTime >= abilityCooldowns.getOrDefault(abilityId, 0L);
    }

    public void putAbilityCooldown(String abilityId, long readyAtGameTime) {
        abilityCooldowns.put(abilityId, readyAtGameTime);
    }

    public static final Codec<HeroData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("activeHero").forGetter(HeroData::activeHero),
            Codec.unboundedMap(Codec.STRING, HeroProgress.CODEC).fieldOf("perHero").forGetter(HeroData::perHero),
            Codec.BOOL.fieldOf("hudTrackerEnabled").forGetter(HeroData::hudTrackerEnabled)
    ).apply(instance, HeroData::new));
}
