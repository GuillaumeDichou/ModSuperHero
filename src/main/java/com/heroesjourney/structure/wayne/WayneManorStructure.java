package com.heroesjourney.structure.wayne;

import com.heroesjourney.structure.HJStructures;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * Dedicated structure for Wayne Manor: places the pre-built {@code wayne_manor.nbt} and
 * {@code wayne_cemetery.nbt} templates together as a single generation (one structure, two
 * pieces sharing one {@link StructurePiecesBuilder}), so rarity/biome placement and the
 * protection zone cover the whole domain at once. This is the only custom structure left in the
 * mod - the "Origine" arc's questline no longer points at Wayne Manor for any quest condition
 * (see the README); it is kept purely as a decorative domain to find.
 */
public class WayneManorStructure extends Structure {

    public static final MapCodec<WayneManorStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(settingsCodec(instance)).apply(instance, WayneManorStructure::new));

    // Template footprints (see wayne_manor.nbt / wayne_cemetery.nbt); both templates face +z (south).
    private static final int MANOR_SIZE_X = 71;
    private static final int MANOR_SIZE_Z = 47;
    private static final int CEMETERY_SIZE_X = 15;
    private static final int CEMETERY_SIZE_Z = 11;

    // The cemetery sits a fixed gap east of the manor, sharing the same ground level and "domain",
    // entrance facing back towards the manor/driveway.
    private static final int CEMETERY_GAP = 15;
    private static final int CEMETERY_OFFSET_X = MANOR_SIZE_X + CEMETERY_GAP;
    private static final int CEMETERY_OFFSET_Z = (MANOR_SIZE_Z - CEMETERY_SIZE_Z) / 2;

    public WayneManorStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        StructureTemplateManager templateManager = context.structureTemplateManager();
        BlockPos chunkCenter = context.chunkPos().getMiddleBlockPosition(0);
        int y = context.chunkGenerator().getFirstFreeHeight(
                chunkCenter.getX(), chunkCenter.getZ(), Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());

        BlockPos manorOrigin = new BlockPos(chunkCenter.getX() - MANOR_SIZE_X / 2, y, chunkCenter.getZ() - MANOR_SIZE_Z / 2);
        BlockPos cemeteryOrigin = manorOrigin.offset(CEMETERY_OFFSET_X, 0, CEMETERY_OFFSET_Z);
        // Reported anchor (used by /locate and treasure maps) is the manor's own centre, which
        // reads far more usefully on a map/compass than a corner of open lawn.
        BlockPos anchor = manorOrigin.offset(MANOR_SIZE_X / 2, 0, MANOR_SIZE_Z / 2);

        return Optional.of(new GenerationStub(anchor, (StructurePiecesBuilder piecesBuilder) -> {
            piecesBuilder.addPiece(new WayneManorPiece(templateManager, manorOrigin));
            piecesBuilder.addPiece(new WayneCemeteryPiece(templateManager, cemeteryOrigin));
        }));
    }

    @Override
    public StructureType<?> type() {
        return HJStructures.WAYNE_MANOR_TYPE.get();
    }
}
