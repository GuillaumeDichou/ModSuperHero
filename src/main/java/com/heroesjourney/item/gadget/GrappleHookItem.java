package com.heroesjourney.item.gadget;

import com.heroesjourney.config.HJConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Fires a hook along the player's line of sight; if it lands on a solid block, the player is
 * accelerated toward the hit point (see {@link GrappleHandler}) until they arrive or cancel by
 * sneaking.
 */
public class GrappleHookItem extends Item {

    public GrappleHookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return InteractionResultHolder.fail(stack);
        }
        double range = HJConfig.GRAPPLE_RANGE.get();
        Vec3 from = player.getEyePosition();
        Vec3 to = from.add(player.getViewVector(1.0F).scale(range));
        HitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        if (hit.getType() == HitResult.Type.MISS) {
            level.playSound(null, player.blockPosition(), SoundEvents.FISHING_BOBBER_THROW.value(), SoundSource.PLAYERS, 0.6F, 1.6F);
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            GrappleHandler.startPull(serverPlayer, hit.getLocation());
        }
        player.getCooldowns().addCooldown(stack.getItem(), HJConfig.GRAPPLE_COOLDOWN_TICKS.get());
        level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_RETURN.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
