package com.heroesjourney.item.armor;

import com.heroesjourney.HeroesJourney;
import java.lang.reflect.Method;
import net.minecraft.world.entity.Entity;

/**
 * Reflection helper backing the bat-suit glide's one non-vanilla behaviour: cancelling an
 * in-progress glide (on sneak - see {@code ArmorEffectsHandler#updateGlideCancel}). A real Elytra
 * has no way to cancel a glide manually at all, so there is no public API for it either -
 * {@link Entity#setSharedFlag(int, boolean)} (what actually flips the fall-flying state vanilla's
 * own movement code reads) is {@code protected}. Safe to reflect on because NeoForge/Forge run on
 * real Mojang mappings at runtime, not just at compile time - this is genuinely the method name in
 * the running game, not just in a decompiled dev view.
 * <p>
 * An earlier version of this class also reflected {@code LivingEntity#jumping} to detect a second
 * jump press as the cancel trigger - that field read turned out to be unreliable once fall-flying
 * was already active (the press wasn't being detected at all), so cancellation was switched to
 * sneak instead, which needs no reflection at all ({@code Player#isShiftKeyDown} is public).
 */
final class GlideSupport {

    private static final int FLAG_FALL_FLYING = 7;

    private static final Method SET_SHARED_FLAG_METHOD = resolveMethod();

    private GlideSupport() {
    }

    /** Directly forces the entity's fall-flying (Elytra gliding) state - used only to force it off on a cancel, never to force it on. */
    static void setFallFlying(Entity entity, boolean value) {
        if (SET_SHARED_FLAG_METHOD == null) {
            return;
        }
        try {
            SET_SHARED_FLAG_METHOD.invoke(entity, FLAG_FALL_FLYING, value);
        } catch (ReflectiveOperationException e) {
            HeroesJourney.LOGGER.error("GlideSupport: failed to invoke Entity#setSharedFlag reflectively", e);
        }
    }

    private static Method resolveMethod() {
        try {
            Method method = Entity.class.getDeclaredMethod("setSharedFlag", int.class, boolean.class);
            method.setAccessible(true);
            HeroesJourney.LOGGER.info("[glide-cancel-debug] GlideSupport: Entity#setSharedFlag resolved successfully via reflection");
            return method;
        } catch (NoSuchMethodException e) {
            HeroesJourney.LOGGER.error("GlideSupport: could not resolve Entity#setSharedFlag via reflection - glide cancel will not work", e);
            return null;
        }
    }
}
