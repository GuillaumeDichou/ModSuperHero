package com.heroesjourney.item;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Purely decorative memorial marker used by quest "proximity to a grave" objectives (e.g. Thomas
 * & Martha Wayne's graves in quest 1). Generic on purpose so future heroes can reuse it for their
 * own memorials - the specific inscription is just the block's translation key/model.
 */
public class HeroGraveBlock extends Block {
    public HeroGraveBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
