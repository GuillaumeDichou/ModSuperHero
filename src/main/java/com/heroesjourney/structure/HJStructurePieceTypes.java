package com.heroesjourney.structure;

import com.heroesjourney.HeroesJourney;
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

    public static final DeferredHolder<StructurePieceType, StructurePieceType> WAYNE_MANOR =
            STRUCTURE_PIECE_TYPES.register("wayne_manor", () -> (StructurePieceType.ContextlessType) WayneManorPiece::new);

    private HJStructurePieceTypes() {
    }
}
