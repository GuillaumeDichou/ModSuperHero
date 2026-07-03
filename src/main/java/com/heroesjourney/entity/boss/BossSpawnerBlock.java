package com.heroesjourney.entity.boss;

import com.heroesjourney.entity.HJEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

/**
 * Placed once per boss arena by the structure generation code. Invisible-ish "brazier" marker
 * that hosts a {@link BossSpawnerBlockEntity} tracking that arena's boss respawn timer. Not meant
 * to be obtained or placed by players (no item form is registered for it).
 */
public class BossSpawnerBlock extends BaseEntityBlock {

    public BossSpawnerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BossSpawnerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, HJEntities.BOSS_SPAWNER_BE.get(), BossSpawnerBlockEntity::serverTick);
    }
}
