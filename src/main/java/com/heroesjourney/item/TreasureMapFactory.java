package com.heroesjourney.item;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Builds a "treasure map" item stack: rather than a fully rendered vanilla map (which would need
 * a lot of client-side map-rendering plumbing to reproduce reliably), this bakes a one-shot
 * direction/distance hint straight into the item's name and lore at the moment it's granted -
 * a deliberate simplification, see the project README.
 */
public final class TreasureMapFactory {

    private TreasureMapFactory() {
    }

    public static ItemStack create(ServerLevel level, BlockPos from, TagKey<Structure> targetStructure, Component mapTitle, int searchRadius) {
        ItemStack stack = new ItemStack(HJItems.TREASURE_MAP.get());
        BlockPos target = level.findNearestMapStructure(targetStructure, from, searchRadius, true);
        stack.set(DataComponents.CUSTOM_NAME, mapTitle);
        if (target == null) {
            stack.set(DataComponents.LORE, new ItemLore(List.of(
                    Component.translatable("item.heroesjourney.treasure_map.unknown").withStyle(net.minecraft.ChatFormatting.GRAY)
            )));
            return stack;
        }
        String direction = compassDirection(from, target);
        double distance = Math.sqrt(from.distSqr(target));
        Component hint = Component.translatable("item.heroesjourney.treasure_map.hint", direction, (int) distance)
                .withStyle(net.minecraft.ChatFormatting.GRAY);
        stack.set(DataComponents.LORE, new ItemLore(List.of(hint)));
        return stack;
    }

    private static String compassDirection(BlockPos from, BlockPos to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double angle = Math.toDegrees(Math.atan2(dx, -dz));
        if (angle < 0) {
            angle += 360;
        }
        String[] labels = {"N", "NE", "E", "SE", "S", "SO", "O", "NO"};
        int index = (int) Math.round(angle / 45.0) % 8;
        return labels[index];
    }
}
