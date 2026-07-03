package com.heroesjourney.entity.boss;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;

/** Maps a boss id (as used by quest conditions/rewards and {@link BossSpawnerBlockEntity}) to its {@link EntityType}. */
public final class BossRegistry {

    private static final Map<String, Supplier<EntityType<?>>> TYPES = new HashMap<>();

    private BossRegistry() {
    }

    public static void clear() {
        TYPES.clear();
    }

    public static void register(String bossId, Supplier<EntityType<?>> type) {
        TYPES.put(bossId, type);
    }

    public static Optional<EntityType<?>> entityType(String bossId) {
        Supplier<EntityType<?>> supplier = TYPES.get(bossId);
        return supplier == null ? Optional.empty() : Optional.ofNullable(supplier.get());
    }
}
