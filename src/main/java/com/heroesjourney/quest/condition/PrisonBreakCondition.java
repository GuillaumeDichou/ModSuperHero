package com.heroesjourney.quest.condition;

import com.heroesjourney.quest.QuestCondition;
import com.heroesjourney.quest.QuestEventContext;
import com.heroesjourney.quest.event.QuestEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Quest 2's two-step objective: find the (correctly-clued) escape key, then reach the exit. This
 * is deliberately one condition rather than two separate objectives, since the "figure out which
 * prisoner is lying" puzzle is resolved by which container the key is hidden in, not by anything
 * the generic engine needs to model. Both steps are checked on the heartbeat (inventory contents
 * / proximity) rather than a pickup event, since "did the player's inventory just gain this item"
 * is a moving target across Minecraft/NeoForge versions.
 */
public class PrisonBreakCondition implements QuestCondition {

    private final Item keyItem;
    private final Block exitMarker;
    private final int exitRadius;

    public PrisonBreakCondition(ResourceLocation keyItemId, Block exitMarker, int exitRadius) {
        this.keyItem = BuiltInRegistries.ITEM.get(keyItemId);
        this.exitMarker = exitMarker;
        this.exitRadius = exitRadius;
    }

    @Override
    public int target() {
        return 2;
    }

    @Override
    public int onEvent(QuestEventContext ctx, int currentProgress) {
        if (!(ctx.event() instanceof QuestEvent.Heartbeat)) {
            return currentProgress;
        }
        if (currentProgress < 1) {
            return ctx.player().getInventory().countItem(keyItem) > 0 ? 1 : currentProgress;
        }
        if (currentProgress == 1 && ConditionUtil.isNear(ctx.level(), ctx.player().blockPosition(), exitMarker, exitRadius)) {
            return 2;
        }
        return currentProgress;
    }

    @Override
    public Component describeProgress(int progress) {
        return switch (progress) {
            case 0 -> Component.translatable("heroesjourney.objective.prison_break.find_key");
            case 1 -> Component.translatable("heroesjourney.objective.prison_break.reach_exit");
            default -> Component.empty();
        };
    }
}
