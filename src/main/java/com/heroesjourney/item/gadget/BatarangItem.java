package com.heroesjourney.item.gadget;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BatarangItem extends Item {

    public BatarangItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            BatarangEntity batarang = new BatarangEntity(level, player);
            batarang.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            batarang.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.6F, 1.0F);
            level.addFreshEntity(batarang);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 0.6F, 1.4F);
        player.getCooldowns().addCooldown(stack.getItem(), 10);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
