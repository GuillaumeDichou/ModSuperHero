package com.heroesjourney.ability;

import java.util.function.Consumer;
import java.util.function.IntSupplier;
import net.minecraft.server.level.ServerPlayer;

/**
 * A keybind-triggered hero ability (e.g. Batman's "Detective Sense"). Registered by content
 * packages, executed by {@code QuestManager#handleUseAbility} once it has checked that the
 * player's active hero has this ability unlocked and off cooldown.
 */
public record Ability(String id, IntSupplier cooldownTicks, Consumer<ServerPlayer> execute) {
}
