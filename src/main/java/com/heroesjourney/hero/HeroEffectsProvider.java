package com.heroesjourney.hero;

import com.heroesjourney.data.HeroProgress;
import net.minecraft.server.level.ServerPlayer;

/**
 * Hooks a hero can implement to grant/refresh/remove its passive gameplay effects (potion
 * effects, attribute modifiers, ...). The generic engine calls these on activation, on
 * deactivation, and roughly once a second while the hero is active - it never needs to know what
 * those effects actually are.
 */
public interface HeroEffectsProvider {

    /** Called once, right when this hero becomes the active hero. */
    default void applyImmediate(ServerPlayer player, HeroProgress progress) {
    }

    /** Called periodically (~1/s) while this hero is active; used to refresh short-lived pulse effects. */
    default void tick(ServerPlayer player, HeroProgress progress) {
    }

    /** Called once, right when this hero stops being the active hero (including on logout). */
    default void clear(ServerPlayer player) {
    }
}
