package com.heroesjourney.structure;

import com.heroesjourney.HeroesJourney;
import com.heroesjourney.structure.wayne.WayneManorStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HJStructures {

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, HeroesJourney.MODID);

    /** Wayne Manor's own dedicated structure type - the only custom structure in the "Origine" arc. */
    public static final DeferredHolder<StructureType<?>, StructureType<WayneManorStructure>> WAYNE_MANOR_TYPE =
            STRUCTURE_TYPES.register("hero_manor_wayne", () -> () -> WayneManorStructure.CODEC);

    // Referenced by data/heroesjourney/tags/worldgen/structure/wayne_manor.json and by
    // WayneManorProtection to find "is this position part of a Wayne Manor domain".
    public static final TagKey<Structure> WAYNE_MANOR = tag("wayne_manor");

    private static TagKey<Structure> tag(String name) {
        return TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, name));
    }

    private HJStructures() {
    }
}
