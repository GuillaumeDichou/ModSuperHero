package com.heroesjourney.structure;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

/**
 * Single generic {@link Structure} implementation reused by all five Batman structures (and any
 * future hero's simple buildings): {@code building_id} selects which {@link BuildingLayout} from
 * {@link BuildingRegistry} to stamp down. Per-structure biome/rarity/placement rules live in the
 * datapack JSON under {@code data/heroesjourney/worldgen/} rather than in Java.
 */
public class HeroBuildingStructure extends Structure {

    public static final MapCodec<HeroBuildingStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            com.mojang.serialization.Codec.STRING.fieldOf("building_id").forGetter(s -> s.buildingId)
    ).apply(instance, HeroBuildingStructure::new));

    private final String buildingId;

    public HeroBuildingStructure(StructureSettings settings, String buildingId) {
        super(settings);
        this.buildingId = buildingId;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        BuildingLayout layout = BuildingRegistry.get(buildingId).orElse(null);
        if (layout == null) {
            return Optional.empty();
        }
        BlockPos chunkCenter = context.chunkPos().getMiddleBlockPosition(0);
        int y = context.chunkGenerator().getFirstFreeHeight(
                chunkCenter.getX(), chunkCenter.getZ(), Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());
        BlockPos origin = new BlockPos(chunkCenter.getX(), y, chunkCenter.getZ());

        return Optional.of(new GenerationStub(origin, (StructurePiecesBuilder piecesBuilder) -> {
            net.minecraft.world.level.levelgen.structure.BoundingBox box = new net.minecraft.world.level.levelgen.structure.BoundingBox(
                    origin.getX() - 1, origin.getY() - 1, origin.getZ() - 1,
                    origin.getX() + layout.sizeX(), origin.getY() + layout.sizeY() + 1, origin.getZ() + layout.sizeZ());
            piecesBuilder.addPiece(new HeroBuildingPiece(HJStructurePieceTypes.HERO_BUILDING.get(), buildingId, origin, box));
        }));
    }

    @Override
    public StructureType<?> type() {
        return HJStructures.HERO_BUILDING.get();
    }
}
