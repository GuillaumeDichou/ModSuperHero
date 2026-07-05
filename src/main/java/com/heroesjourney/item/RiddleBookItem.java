package com.heroesjourney.item;

import com.heroesjourney.item.puzzle.PuzzleGenerator;
import com.heroesjourney.network.HJNetworking;
import com.heroesjourney.quest.QuestManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Quest-4 reward item: right-click opens the next puzzle in the fixed 3-puzzle sequence (sliding
 * tiles, then lock-picking, then Mastermind) - which one depends on how many the player has
 * already solved for their current puzzle objective, so closing and reopening the book resumes
 * where they left off instead of restarting the sequence.
 */
public class RiddleBookItem extends Item {

    public RiddleBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            int sequenceIndex = QuestManager.INSTANCE.currentPuzzleProgress(serverPlayer);
            HJNetworking.sendToPlayer(serverPlayer, PuzzleGenerator.generateSequenced(sequenceIndex, serverPlayer.getRandom()));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
