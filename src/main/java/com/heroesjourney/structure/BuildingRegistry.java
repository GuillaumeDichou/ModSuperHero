package com.heroesjourney.structure;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class BuildingRegistry {

    private static final Map<String, BuildingLayout> LAYOUTS = new HashMap<>();

    private BuildingRegistry() {
    }

    public static void clear() {
        LAYOUTS.clear();
    }

    public static void register(String id, BuildingLayout layout) {
        LAYOUTS.put(id, layout);
    }

    public static Optional<BuildingLayout> get(String id) {
        return Optional.ofNullable(LAYOUTS.get(id));
    }
}
