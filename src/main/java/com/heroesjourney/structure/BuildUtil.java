package com.heroesjourney.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Small block-placement helpers shared by structure pieces. Every method silently no-ops for
 * positions outside the chunk currently being processed (the standard "only touch this chunk"
 * pattern for {@code StructurePiece#postProcess}), so callers never need to check bounds
 * themselves.
 */
public final class BuildUtil {

    private BuildUtil() {
    }

    public static void set(WorldGenLevel level, BoundingBox chunkBox, int x, int y, int z, BlockState state) {
        BlockPos pos = new BlockPos(x, y, z);
        if (chunkBox.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    /** Inclusive solid box fill. */
    public static void fill(WorldGenLevel level, BoundingBox chunkBox, int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        int minX = Math.min(x0, x1), maxX = Math.max(x0, x1);
        int minY = Math.min(y0, y1), maxY = Math.max(y0, y1);
        int minZ = Math.min(z0, z1), maxZ = Math.max(z0, z1);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    cursor.set(x, y, z);
                    if (chunkBox.isInside(cursor)) {
                        level.setBlock(cursor, state, 2);
                    }
                }
            }
        }
    }

    /** Inclusive hollow box: 1-thick walls/floor/ceiling only, air inside. */
    public static void hollow(WorldGenLevel level, BoundingBox chunkBox, int x0, int y0, int z0, int x1, int y1, int z1, BlockState wallState) {
        int minX = Math.min(x0, x1), maxX = Math.max(x0, x1);
        int minY = Math.min(y0, y1), maxY = Math.max(y0, y1);
        int minZ = Math.min(z0, z1), maxZ = Math.max(z0, z1);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean edge = x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ;
                    if (!edge) {
                        continue;
                    }
                    cursor.set(x, y, z);
                    if (chunkBox.isInside(cursor)) {
                        level.setBlock(cursor, wallState, 2);
                    }
                }
            }
        }
    }

    /** Flat horizontal rectangle (a floor, ceiling, or path slab) at a single Y level. */
    public static void plate(WorldGenLevel level, BoundingBox chunkBox, int x0, int z0, int x1, int z1, int y, BlockState state) {
        fill(level, chunkBox, x0, y, z0, x1, y, z1, state);
    }
}
