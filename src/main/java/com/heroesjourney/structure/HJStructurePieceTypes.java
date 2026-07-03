package com.heroesjourney.structure;

import com.heroesjourney.HeroesJourney;
import com.heroesjourney.structure.wayne.WayneCemeteryPiece;
import com.heroesjourney.structure.wayne.WayneManorPiece;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HJStructurePieceTypes {

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, HeroesJourney.MODID);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> HERO_BUILDING =
            STRUCTURE_PIECE_TYPES.register("hero_building", () -> (StructurePieceType.ContextlessType) HeroBuildingPiece::new);

    // Both Wayne pieces are NBT-template based (TemplateStructurePiece) and need the
    // StructureTemplateManager from the serialization context on load, so they use the base
    // StructurePieceType functional interface (context + tag) rather than ContextlessType.
    public static final DeferredHolder<StructurePieceType, StructurePieceType> WAYNE_MANOR =
            STRUCTURE_PIECE_TYPES.register("wayne_manor", () -> WayneManorPiece::new);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> WAYNE_CEMETERY =
            STRUCTURE_PIECE_TYPES.register("wayne_cemetery", () -> WayneCemeteryPiece::new);

    private HJStructurePieceTypes() {
    }
}
