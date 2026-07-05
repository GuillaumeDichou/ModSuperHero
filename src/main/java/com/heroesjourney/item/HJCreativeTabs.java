package com.heroesjourney.item;

import com.heroesjourney.HeroesJourney;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HJCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HeroesJourney.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> HEROES_JOURNEY_TAB = TABS.register("heroesjourney",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.heroesjourney"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> new ItemStack(HJItems.BAT_ARMORED_CHESTPLATE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(HJItems.KEVLAR_FIBER.get());
                        output.accept(HJItems.MEMORY_CLOTH.get());
                        output.accept(HJItems.ELECTRONIC_COMPONENT.get());
                        output.accept(HJItems.BAT_COMBAT_VEST.get());
                        output.accept(HJItems.BAT_CAPE.get());
                        output.accept(HJItems.BAT_COWL.get());
                        output.accept(HJItems.BAT_ARMORED_CHESTPLATE.get());
                        output.accept(HJItems.BAT_LEGGINGS.get());
                        output.accept(HJItems.BAT_BOOTS.get());
                        output.accept(HJItems.BATARANG.get());
                        output.accept(HJItems.GRAPPLE_HOOK.get());
                        output.accept(HJItems.SMOKE_PEBBLE.get());
                        output.accept(HJItems.RIDDLE_BOOK.get());
                        output.accept(HJItems.WAYNE_GRAVE_THOMAS_ITEM.get());
                        output.accept(HJItems.WAYNE_GRAVE_MARTHA_ITEM.get());
                    }).build());

    private HJCreativeTabs() {
    }
}
