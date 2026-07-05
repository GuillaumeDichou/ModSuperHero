package com.heroesjourney.item.gadget;

import com.heroesjourney.HeroesJourney;
import com.heroesjourney.config.HJConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Fires a hook along the player's line of sight; if it lands on a solid block, the player is
 * accelerated toward the hit point (see {@link GrappleHandler}) until they arrive or cancel by
 * sneaking.
 * <p>
 * Needs both {@link #use} (nothing in short reach - the common case at typical grapple ranges)
 * and {@link #useOn} (a block IS within short reach, e.g. firing at a nearby wall or ceiling):
 * vanilla dispatches right-click to {@code useOn} instead of {@code use} whenever the crosshair
 * hits a block within normal interaction distance, so relying on {@code use} alone meant the
 * grapple silently did nothing at close range.
 */
public class GrappleHookItem extends Item {

    public GrappleHookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        HeroesJourney.LOGGER.info("[grapple-debug] {} useOn() called, clickedPos={}, onCooldown={}",
                player.getGameProfile().getName(), context.getClickedPos(), player.getCooldowns().isOnCooldown(stack.getItem()));
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return InteractionResult.FAIL;
        }
        BlockPos pos = context.getClickedPos();
        fireGrapple(level, player, stack, Vec3.atCenterOf(pos));
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        HeroesJourney.LOGGER.info("[grapple-debug] {} use() called, onCooldown={}",
                player.getGameProfile().getName(), player.getCooldowns().isOnCooldown(stack.getItem()));
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return InteractionResultHolder.fail(stack);
        }
        double range = HJConfig.GRAPPLE_RANGE.get();
        Vec3 from = player.getEyePosition();
        Vec3 to = from.add(player.getViewVector(1.0F).scale(range));
        HitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        HeroesJourney.LOGGER.info("[grapple-debug] {} use() raytrace result={} location={}",
                player.getGameProfile().getName(), hit.getType(), hit.getLocation());

        if (hit.getType() == HitResult.Type.MISS) {
            level.playSound(null, player.blockPosition(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 0.6F, 1.6F);
            return InteractionResultHolder.fail(stack);
        }

        fireGrapple(level, player, stack, hit.getLocation());
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private void fireGrapple(Level level, Player player, ItemStack stack, Vec3 target) {
        HeroesJourney.LOGGER.info("[grapple-debug] {} fireGrapple target={} isClientSide={} isServerPlayer={}",
                player.getGameProfile().getName(), target, level.isClientSide, player instanceof ServerPlayer);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            GrappleHandler.startPull(serverPlayer, target);
        }
        player.getCooldowns().addCooldown(stack.getItem(), HJConfig.GRAPPLE_COOLDOWN_TICKS.get());
        level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 0.8F, 0.8F);
    }
}
