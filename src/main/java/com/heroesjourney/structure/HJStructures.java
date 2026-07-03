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

    public static final DeferredHolder<StructureType<?>, StructureType<HeroBuildingStructure>> HERO_BUILDING =
            STRUCTURE_TYPES.register("hero_building", () -> () -> HeroBuildingStructure.CODEC);

    /** Wayne Manor's own dedicated structure type - see {@link WayneManorStructure} for why it isn't on the generic system. */
    public static final DeferredHolder<StructureType<?>, StructureType<WayneManorStructure>> WAYNE_MANOR_TYPE =
            STRUCTURE_TYPES.register("hero_manor_wayne", () -> () -> WayneManorStructure.CODEC);

    // Structure tags referenced both by the structure_set/structure JSON (data/heroesjourney/tags/worldgen/structure)
    // and by TreasureMapFactory to find "the nearest X" for quest rewards.
    public static final TagKey<Structure> WAYNE_MANOR = tag("wayne_manor");
    public static final TagKey<Structure> PRISON = tag("prison");
    public static final TagKey<Structure> MONASTERY = tag("monastery");
    public static final TagKey<Structure> ASYLUM = tag("asylum");
    public static final TagKey<Structure> TRAIN = tag("train");

    private static TagKey<Structure> tag(String name) {
        return TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, name));
    }

    private HJStructures() {
    }
}
