package com.heroesjourney.hero;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Simple in-memory registry of {@link HeroDefinition}s, populated during common setup by each
 * hero's content class (e.g. {@code BatmanContent}). Not a Minecraft {@code Registry} on
 * purpose: heroes are metadata + a questline, not game objects that need to be referenced from
 * data packs.
 */
public final class HeroRegistry {

    /** Sentinel id meaning "no hero currently active". */
    public static final String NONE = "none";

    private static final Map<String, HeroDefinition> HEROES = new LinkedHashMap<>();

    private HeroRegistry() {
    }

    public static void clear() {
        HEROES.clear();
    }

    public static void register(HeroDefinition definition) {
        if (HEROES.containsKey(definition.id())) {
            throw new IllegalStateException("Duplicate hero id: " + definition.id());
        }
        HEROES.put(definition.id(), definition);
    }

    public static Optional<HeroDefinition> get(String id) {
        return Optional.ofNullable(HEROES.get(id));
    }

    public static Collection<HeroDefinition> all() {
        return HEROES.values();
    }
}
