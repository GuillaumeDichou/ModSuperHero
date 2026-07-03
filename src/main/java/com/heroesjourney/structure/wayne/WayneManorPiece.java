package com.heroesjourney.structure.wayne;

import com.heroesjourney.HeroesJourney;
import com.heroesjourney.structure.HJStructurePieceTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * Places the pre-built {@code wayne_manor.nbt} template (71x28x47, facade facing +z/south, y=0
 * layer is the template's own lawn meant to sit at ground level). Building on vanilla's
 * {@link TemplateStructurePiece} - the same base class vanilla uses for Ruined Portals, igloos,
 * ocean ruins, etc. - means the tricky "load and stamp an NBT template into the world" logic is
 * vanilla's own proven code, not something hand-rolled here.
 */
public class WayneManorPiece extends TemplateStructurePiece {

    public static final ResourceLocation TEMPLATE_ID = ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, "wayne_manor");

    /** Extra margin (blocks) added around the template's own footprint for the Wayne Manor protection zone. */
    private static final int PROTECTION_MARGIN = 8;

    public WayneManorPiece(StructureTemplateManager templateManager, BlockPos pos) {
        super(HJStructurePieceTypes.WAYNE_MANOR.get(), 0, templateManager, TEMPLATE_ID, TEMPLATE_ID.toString(), settings(), pos);
        inflateBoundingBox();
    }

    public WayneManorPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(HJStructurePieceTypes.WAYNE_MANOR.get(), tag, context.structureTemplateManager(), id -> settings());
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
    protected void handleDataMarker(String marker, BlockPos pos, ServerLevelAccessor level, RandomSource random, BoundingBox box) {
        // The template has no embedded data markers (structure blocks in DATA mode) today;
        // reserved for future use (e.g. an NPC or loot spawn point placed by name in-editor).
    }
}
