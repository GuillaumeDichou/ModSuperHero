package com.heroesjourney.ability;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class AbilityRegistry {

    private static final Map<String, Ability> ABILITIES = new HashMap<>();

    private AbilityRegistry() {
    }

    public static void clear() {
        ABILITIES.clear();
    }

    public static void register(Ability ability) {
        ABILITIES.put(ability.id(), ability);
    }

    public static Optional<Ability> get(String id) {
        return Optional.ofNullable(ABILITIES.get(id));
    }
}
