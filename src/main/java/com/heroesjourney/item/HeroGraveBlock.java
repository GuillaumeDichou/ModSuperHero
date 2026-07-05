package com.heroesjourney.item;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Purely decorative memorial marker (e.g. Thomas &amp; Martha Wayne's graves in the Wayne Manor
 * cemetery). Generic on purpose so future heroes/quests can reuse it for their own memorials, or
 * reintroduce a "proximity to a grave" objective without new block classes - the specific
 * inscription is just the block's translation key/model.
 */
public class HeroGraveBlock extends Block {
    public HeroGraveBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
