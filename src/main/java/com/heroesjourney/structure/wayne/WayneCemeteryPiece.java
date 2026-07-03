package com.heroesjourney.structure.wayne;

import com.heroesjourney.HeroesJourney;
import com.heroesjourney.item.HJItems;
import com.heroesjourney.structure.HJStructurePieceTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.TemplateStructurePiece;

/**
 * Places the pre-built {@code wayne_cemetery.nbt} template (15x6x11, entrance facing +z/south)
 * and then overlays two of the mod's own marker blocks ({@code heroesjourney:wayne_grave_thomas}
 * / {@code _martha}) exactly on top of the template's two steles (local positions (5,1,3) and
 * (9,1,3), per the template's own design).
 * <p>
 * This is the "explicit anchor" the quest-1 grave-proximity objective relies on: the template's
 * own stele blocks are plain deepslate with nothing unique to search for, so
 * {@code ProximityToBlockCondition} needs an actual marker - and since this class computes the
 * marker positions directly from {@link #templatePosition} (this piece's own real placement, set
 * once by {@link WayneManorStructure#findGenerationPoint}), there is no coordinate offset copied
 * from the manor or hard-coded anywhere else to drift out of sync.
 */
public class WayneCemeteryPiece extends TemplateStructurePiece {

    public static final ResourceLocation TEMPLATE_ID = ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, "wayne_cemetery");

    private static final BlockPos THOMAS_LOCAL_POS = new BlockPos(5, 1, 3);
    private static final BlockPos MARTHA_LOCAL_POS = new BlockPos(9, 1, 3);

    private static final int PROTECTION_MARGIN = 8;

    public WayneCemeteryPiece(StructureTemplateManager templateManager, BlockPos pos) {
        super(HJStructurePieceTypes.WAYNE_CEMETERY.get(), 0, templateManager, TEMPLATE_ID, TEMPLATE_ID.toString(), settings(), pos);
        inflateBoundingBox();
    }

    public WayneCemeteryPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(HJStructurePieceTypes.WAYNE_CEMETERY.get(), tag, context.structureTemplateManager(), id -> settings());
        inflateBoundingBox();
    }

    private static StructurePlaceSettings settings() {
        return new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false);
    }

    private void inflateBoundingBox() {
        BoundingBox box = this.boundingBox;
        this.boundingBox = new BoundingBox(
                box.minX() - PROTECTION_MARGIN, box.minY(), box.minZ() - PROTECTION_MARGIN,
                box.maxX() + PROTECTION_MARGIN, box.maxY(), box.maxZ() + PROTECTION_MARGIN);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                             RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        super.postProcess(level, structureManager, generator, random, chunkBox, chunkPos, pivot);
        placeGraveMarker(level, chunkBox, THOMAS_LOCAL_POS, HJItems.WAYNE_GRAVE_THOMAS.get().defaultBlockState());
        placeGraveMarker(level, chunkBox, MARTHA_LOCAL_POS, HJItems.WAYNE_GRAVE_MARTHA.get().defaultBlockState());
    }

    private void placeGraveMarker(WorldGenLevel level, BoundingBox chunkBox, BlockPos localOffset, BlockState marker) {
        BlockPos worldPos = this.templatePosition.offset(localOffset);
        if (chunkBox.isInside(worldPos)) {
            level.setBlock(worldPos, marker, 2);
        }
    }

    @Override
    protected void handleDataMarker(String marker, BlockPos pos, ServerLevelAccessor level, RandomSource random, BoundingBox box) {
        // No embedded data markers in this template.
    }
}
