package com.heroesjourney.structure;

/**
 * A single "thing to place" inside a building, at coordinates relative to the structure's own
 * origin (its south-west floor corner). Kept as one small generic type so any future hero's
 * structures can reuse the same placer.
 */
public record BuildingFeature(int dx, int dy, int dz, Kind kind, String param) {

    public enum Kind {
        GRAVE_THOMAS,
        GRAVE_MARTHA,
        BOSS_SPAWNER,
        NPC_SPAWN,
        HOSTILE_SPAWN,
        CHEST,
        TORCH
    }

    public static BuildingFeature of(int dx, int dy, int dz, Kind kind) {
        return new BuildingFeature(dx, dy, dz, kind, "");
    }

    public static BuildingFeature of(int dx, int dy, int dz, Kind kind, String param) {
        return new BuildingFeature(dx, dy, dz, kind, param);
    }
}
