package com.heroesjourney.quest.condition;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

final class ConditionUtil {

    private ConditionUtil() {
    }

    static boolean isNear(ServerLevel level, BlockPos center, Block target, int radius) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (level.getBlockState(cursor).is(target)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
