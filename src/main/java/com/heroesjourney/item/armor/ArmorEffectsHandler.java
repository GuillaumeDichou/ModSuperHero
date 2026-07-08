package com.heroesjourney.item.armor;

import com.heroesjourney.config.HJConfig;
import com.heroesjourney.content.batman.BatmanAbilities;
import com.heroesjourney.data.HJAttachments;
import com.heroesjourney.data.HeroData;
import com.heroesjourney.item.HJItems;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Live, per-event/per-tick gating of the bat-suit's intrinsic effects: fall-damage reduction (bat
 * leggings), night vision (cowl), and the glide-cancel behaviour (chestplate).
 * <p>
 * Night vision is granted ONCE (a very long, effectively-permanent duration) the tick the cowl
 * becomes worn while Batman is active, not re-granted every tick with a short duration - re-granting
 * a short duration every tick is what previously kept the remaining duration permanently inside
 * vanilla's "about to expire" HUD-flash window (that window is ~200 ticks; a duration that never
 * exceeds it never stops flashing, no matter how often it's refreshed). It is still removed
 * explicitly and instantly - checked every tick - the moment the cowl comes off or Batman stops
 * being active, rather than left to expire.
 * <p>
 * The glide itself starts and persists entirely through vanilla's own Elytra toggle logic (a single
 * jump-while-falling triggers it, and it then keeps going on its own, exactly like a real Elytra) -
 * see {@code BatChestplateItem#canElytraFly}/{@code #elytraFlightTick}. The only thing driven from
 * here is the one behaviour vanilla doesn't have: cancelling the glide manually, on sneak (see
 * {@link #updateGlideCancel}) - detected as a rising edge (sneak newly pressed, not merely held) so
 * a player who was already sneaking when the glide started doesn't get it cancelled instantly.
 */
public final class ArmorEffectsHandler {

    /** Effectively permanent - removal is always explicit (see class doc), never left to expire. */
    private static final int NIGHT_VISION_DURATION_TICKS = Integer.MAX_VALUE;

    private final Map<UUID, Boolean> wasSneaking = new HashMap<>();

    @SubscribeEvent
    public void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!isBatmanActive(player)) {
            return;
        }
        if (player.getItemBySlot(EquipmentSlot.LEGS).is(HJItems.BAT_LEGGINGS.get())) {
            event.setDamageMultiplier(event.getDamageMultiplier() * (1.0F - HJConfig.FALL_DAMAGE_REDUCTION.get().floatValue()));
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        boolean batmanActive = isBatmanActive(player);

        boolean shouldHaveNightVision = batmanActive && player.getItemBySlot(EquipmentSlot.HEAD).is(HJItems.BAT_COWL.get());
        if (shouldHaveNightVision) {
            if (!player.hasEffect(MobEffects.NIGHT_VISION)) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, NIGHT_VISION_DURATION_TICKS, 0, true, false, false));
            }
        } else if (player.hasEffect(MobEffects.NIGHT_VISION)) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }

        updateGlideCancel(player, batmanActive);
    }

    private void updateGlideCancel(ServerPlayer player, boolean batmanActive) {
        UUID id = player.getUUID();
        boolean sneaking = player.isShiftKeyDown();
        boolean justStartedSneaking = sneaking && !wasSneaking.getOrDefault(id, false);
        wasSneaking.put(id, sneaking);

        if (!justStartedSneaking || !player.isFallFlying()) {
            return;
        }
        boolean wearingChestplate = player.getItemBySlot(EquipmentSlot.CHEST).is(HJItems.BAT_ARMORED_CHESTPLATE.get());
        if (batmanActive && wearingChestplate) {
            GlideSupport.setFallFlying(player, false);
        }
    }

    @SubscribeEvent
    public void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        wasSneaking.remove(event.getEntity().getUUID());
    }

    private boolean isBatmanActive(ServerPlayer player) {
        HeroData data = player.getData(HJAttachments.HERO_DATA);
        return data.hasActiveHero() && data.activeHero().equals(BatmanAbilities.HERO_ID);
    }
}
