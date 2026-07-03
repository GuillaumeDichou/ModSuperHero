package com.heroesjourney.structure;

import com.heroesjourney.entity.HJEntities;
import com.heroesjourney.entity.boss.BossSpawnerBlockEntity;
import com.heroesjourney.entity.npc.QuestNpcEntity;
import com.heroesjourney.item.HJItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureManager;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.WorldGenLevel;

/**
 * Generic "stamp a simple box building into the world" piece, shared by every structure in the
 * Batman arc (and reusable by future heroes). Deliberately doesn't rotate/mirror - every building
 * faces the same way - which keeps the placement code simple and reliable at the cost of visual
 * variety; see the README.
 */
public class HeroBuildingPiece extends StructurePiece {

    private final String buildingId;
    private final BlockPos origin;

    public HeroBuildingPiece(StructurePieceType type, String buildingId, BlockPos origin, BoundingBox box) {
        super(type, 0, box);
        this.buildingId = buildingId;
        this.origin = origin;
    }

    public HeroBuildingPiece(CompoundTag tag) {
        super(HJStructurePieceTypes.HERO_BUILDING.get(), tag);
        this.buildingId = tag.getString("BuildingId");
        this.origin = new BlockPos(tag.getInt("OriginX"), tag.getInt("OriginY"), tag.getInt("OriginZ"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("BuildingId", buildingId);
        tag.putInt("OriginX", origin.getX());
        tag.putInt("OriginY", origin.getY());
        tag.putInt("OriginZ", origin.getZ());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                             RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        BuildingLayout layout = BuildingRegistry.get(buildingId).orElse(null);
        if (layout == null) {
            return;
        }
        int sx = layout.sizeX();
        int sy = layout.sizeY();
        int sz = layout.sizeZ();

        BlockState floor = layout.floorBlock().defaultBlockState();
        BlockState wall = layout.wallBlock().defaultBlockState();
        BlockState roof = layout.roofBlock().defaultBlockState();
        BlockState accent = layout.accentBlock().defaultBlockState();
        BlockState air = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();

        // Foundation + floor.
        for (int x = -1; x <= sx; x++) {
            for (int z = -1; z <= sz; z++) {
                place(level, chunkBox, origin.offset(x, -1, z), wall);
                if (x >= 0 && x < sx && z >= 0 && z < sz) {
                    place(level, chunkBox, origin.offset(x, 0, z), floor);
                }
            }
        }

        // Walls, with a door gap centred on the south face (z = 0) and simple window gaps.
        int doorX = sx / 2;
        for (int y = 1; y <= sy; y++) {
            for (int x = 0; x < sx; x++) {
                for (int z = 0; z < sz; z++) {
                    boolean edge = x == 0 || z == 0 || x == sx - 1 || z == sz - 1;
                    if (!edge) {
                        continue;
                    }
                    boolean isDoor = z == 0 && (x == doorX || x == doorX + 1) && y <= 2;
                    boolean isCorner = (x == 0 || x == sx - 1) && (z == 0 || z == sz - 1);
                    boolean isWindow = layout.windows() && !isCorner && y == Math.max(2, sy / 2) && ((x + z) % 4 == 0);
                    if (isDoor || isWindow) {
                        place(level, chunkBox, origin.offset(x, y, z), air);
                    } else {
                        place(level, chunkBox, origin.offset(x, y, z), isCorner ? accent : wall);
                    }
                }
            }
        }

        // Flat roof.
        for (int x = 0; x < sx; x++) {
            for (int z = 0; z < sz; z++) {
                place(level, chunkBox, origin.offset(x, sy + 1, z), roof);
            }
        }

        for (BuildingFeature feature : layout.features()) {
            BlockPos pos = origin.offset(feature.dx(), feature.dy(), feature.dz());
            if (chunkBox.isInside(pos) && level instanceof ServerLevel) {
                applyFeature((ServerLevel) level, pos, feature);
            }
        }
    }

    private void place(WorldGenLevel level, BoundingBox chunkBox, BlockPos pos, BlockState state) {
        if (chunkBox.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private void applyFeature(ServerLevel level, BlockPos pos, BuildingFeature feature) {
        switch (feature.kind()) {
            case GRAVE_THOMAS -> level.setBlock(pos, HJItems.WAYNE_GRAVE_THOMAS.get().defaultBlockState(), 2);
            case GRAVE_MARTHA -> level.setBlock(pos, HJItems.WAYNE_GRAVE_MARTHA.get().defaultBlockState(), 2);
            case BOSS_SPAWNER -> {
                level.setBlock(pos, HJItems.BOSS_SPAWNER_BLOCK.get().defaultBlockState(), 2);
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof BossSpawnerBlockEntity spawner) {
                    spawner.configure(feature.param());
                }
                spawnEntity(level, pos, resolveBossType(feature.param())).ifPresent(entity -> {
                    if (entity instanceof com.heroesjourney.entity.boss.HeroBossEntity boss) {
                        boss.setHomeSpawner(pos);
                    }
                    if (be instanceof BossSpawnerBlockEntity spawner) {
                        spawner.notifyBossSpawned(entity.getUUID());
                    }
                });
            }
            case NPC_SPAWN -> spawnEntity(level, pos, HJEntities.QUEST_NPC.get()).ifPresent(entity -> {
                if (entity instanceof QuestNpcEntity npc) {
                    npc.setNpcId(feature.param());
                }
            });
            case HOSTILE_SPAWN -> spawnEntity(level, pos, "ninja".equals(feature.param()) ? HJEntities.NINJA.get() : HJEntities.PRISON_GUARD.get());
            case CHEST -> {
                level.setBlock(pos, net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState(), 2);
                if (!feature.param().isEmpty()) {
                    BlockEntity chestBe = level.getBlockEntity(pos);
                    if (chestBe instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest) {
                        net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                                net.minecraft.resources.ResourceLocation.parse(feature.param()));
                        chest.setItem(0, new net.minecraft.world.item.ItemStack(item));
                    }
                }
            }
            case TORCH -> level.setBlock(pos, net.minecraft.world.level.block.Blocks.TORCH.defaultBlockState(), 2);
        }
    }

    private java.util.Optional<Entity> spawnEntity(ServerLevel level, BlockPos pos, EntityType<?> type) {
        Entity entity = type.create(level);
        if (entity == null) {
            return java.util.Optional.empty();
        }
        entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        level.addFreshEntity(entity);
        return java.util.Optional.of(entity);
    }

    private EntityType<?> resolveBossType(String bossId) {
        return com.heroesjourney.entity.boss.BossRegistry.entityType(bossId).orElse(HJEntities.NINJA.get());
    }
}
