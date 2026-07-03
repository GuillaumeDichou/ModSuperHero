package com.heroesjourney.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mutable, per-player, per-hero progress: which stage the player is on, how far along its
 * objectives are, and any flags/abilities that stage's rewards unlocked. One instance lives per
 * hero id inside {@link HeroData#perHero()}, so switching heroes never loses progress.
 */
public final class HeroProgress {

    private int stageIndex;
    private final Map<String, Integer> objectiveProgress;
    private final Set<String> flags;
    private final Set<String> unlockedAbilities;

    public HeroProgress() {
        this(0, new HashMap<>(), new HashSet<>(), new HashSet<>());
    }

    public HeroProgress(int stageIndex, Map<String, Integer> objectiveProgress, Set<String> flags, Set<String> unlockedAbilities) {
        this.stageIndex = stageIndex;
        this.objectiveProgress = new HashMap<>(objectiveProgress);
        this.flags = new HashSet<>(flags);
        this.unlockedAbilities = new HashSet<>(unlockedAbilities);
    }

    public int stageIndex() {
        return stageIndex;
    }

    public void advanceStage() {
        stageIndex++;
    }

    public void setStageIndex(int index) {
        this.stageIndex = index;
    }

    public Map<String, Integer> objectiveProgress() {
        return objectiveProgress;
    }

    public int progressFor(String objectiveKey) {
        return objectiveProgress.getOrDefault(objectiveKey, 0);
    }

    public void setProgress(String objectiveKey, int value) {
        objectiveProgress.put(objectiveKey, value);
    }

    public Set<String> flags() {
        return flags;
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    public void addFlag(String flag) {
        flags.add(flag);
    }

    public Set<String> unlockedAbilities() {
        return unlockedAbilities;
    }

    public boolean hasAbility(String abilityId) {
        return unlockedAbilities.contains(abilityId);
    }

    public void unlockAbility(String abilityId) {
        unlockedAbilities.add(abilityId);
    }

    public static final Codec<HeroProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("stageIndex").forGetter(HeroProgress::stageIndex),
            Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("objectiveProgress").forGetter(HeroProgress::objectiveProgress),
            Codec.STRING.listOf().xmap(list -> (Set<String>) new HashSet<>(list), ArrayList::new).fieldOf("flags").forGetter(HeroProgress::flags),
            Codec.STRING.listOf().xmap(list -> (Set<String>) new HashSet<>(list), ArrayList::new).fieldOf("unlockedAbilities").forGetter(HeroProgress::unlockedAbilities)
    ).apply(instance, HeroProgress::new));
}
