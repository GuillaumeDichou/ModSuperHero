package com.heroesjourney.structure.wayne;

import com.heroesjourney.structure.HJStructurePieceTypes;
import com.heroesjourney.structure.HJStructures;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

/**
 * Dedicated structure for Wayne Manor: a full estate (manor, garden, iron-and-stone perimeter
 * fence, driveway, family cemetery) built by {@link WayneManorPiece}. Kept separate from the
 * generic {@code HeroBuildingStructure}/{@code HeroBuildingPiece} system used by the other four
 * Batman structures - Wayne Manor is unique and detailed enough to deserve bespoke code, while
 * prison/monastery/asylum/train stay on the simple, reusable "box building" system.
 */
public class WayneManorStructure extends Structure {

    public static final MapCodec<WayneManorStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(settingsCodec(instance)).apply(instance, WayneManorStructure::new));

    public WayneManorStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        BlockPos chunkCenter = context.chunkPos().getMiddleBlockPosition(0);
        int y = context.chunkGenerator().getFirstFreeHeight(
                chunkCenter.getX(), chunkCenter.getZ(), Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());
        // The estate origin is the south-west/bottom corner of its footprint; the reported
        // "anchor" (used by /locate and treasure maps) is the manor's own centre instead, which
        // reads far more usefully on a map or compass than a corner of open garden.
        BlockPos origin = new BlockPos(chunkCenter.getX() - WayneManorPiece.ESTATE_SIZE_X / 2, y, chunkCenter.getZ() - WayneManorPiece.ESTATE_SIZE_Z / 2);
        BlockPos anchor = origin.offset(WayneManorPiece.MANOR_ORIGIN_X + WayneManorPiece.MANOR_SIZE_X / 2, 0,
                WayneManorPiece.MANOR_ORIGIN_Z + WayneManorPiece.MANOR_SIZE_Z / 2);

        return Optional.of(new GenerationStub(anchor, (StructurePiecesBuilder piecesBuilder) -> {
            // Generous vertical padding: gardens/driveway are graded (see WayneManorPiece#levelGround)
            // well below and above the sampled ground height, and the roof ridge rises a good way
            // above it too, so the piece's own declared bounding box - which is what determines
            // which chunks even get a postProcess callback for this piece - must comfortably
            // cover all of that, not just the footprint at ground level.
            BoundingBox box = new BoundingBox(
                    origin.getX() - 1, origin.getY() - 12, origin.getZ() - 1,
                    origin.getX() + WayneManorPiece.ESTATE_SIZE_X + 1, origin.getY() + 40, origin.getZ() + WayneManorPiece.ESTATE_SIZE_Z + 1);
            piecesBuilder.addPiece(new WayneManorPiece(HJStructurePieceTypes.WAYNE_MANOR.get(), origin, box));
        }));
    }

    @Override
    public StructureType<?> type() {
        return HJStructures.WAYNE_MANOR_TYPE.get();
    }
}
