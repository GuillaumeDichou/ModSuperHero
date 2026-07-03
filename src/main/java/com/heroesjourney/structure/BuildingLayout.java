package com.heroesjourney.structure;

import java.util.List;
import net.minecraft.world.level.block.Block;

/**
 * Everything needed to stamp one simple building into the world: its footprint, block palette
 * and the list of {@link BuildingFeature}s placed inside it (graves, boss anchor, NPC/mob
 * spawns, loot chests). Deliberately a plain box with a flat roof rather than the elaborate
 * multi-storey buildings described in the design doc - see the README for why.
 */
public record BuildingLayout(
        int sizeX,
        int sizeY,
        int sizeZ,
        Block wallBlock,
        Block floorBlock,
        Block roofBlock,
        Block accentBlock,
        boolean windows,
        List<BuildingFeature> features
) {
}
