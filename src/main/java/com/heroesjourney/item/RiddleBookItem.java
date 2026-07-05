package com.heroesjourney.item;

import com.heroesjourney.item.puzzle.PuzzleGenerator;
import com.heroesjourney.network.HJNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Quest-4 reward item: right-click opens a random one of the three riddle-book mini-games. */
public class RiddleBookItem extends Item {

    public RiddleBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            HJNetworking.sendToPlayer(serverPlayer, PuzzleGenerator.generate(serverPlayer.getRandom()));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
