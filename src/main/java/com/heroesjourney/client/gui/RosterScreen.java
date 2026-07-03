package com.heroesjourney.client.gui;

import com.heroesjourney.client.ClientHeroDataCache;
import com.heroesjourney.data.HeroData;
import com.heroesjourney.data.HeroProgress;
import com.heroesjourney.hero.HeroDefinition;
import com.heroesjourney.hero.HeroRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Screen 1: the roster. Lists every registered hero with its status (locked / available / active
 * / completed); today that is just Batman Nolan, but the layout is a plain scrolling list so
 * adding more heroes later is purely a content change.
 */
public class RosterScreen extends Screen {

    private static final int ROW_HEIGHT = 28;

    public RosterScreen() {
        super(Component.translatable("gui.heroesjourney.roster.title"));
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(320, this.width - 40);
        int left = (this.width - panelWidth) / 2;
        int top = 40;
        int index = 0;
        for (HeroDefinition hero : HeroRegistry.all()) {
            int rowTop = top + index * ROW_HEIGHT;
            Button button = Button.builder(rowLabel(hero), b -> this.minecraft.setScreen(new HeroDetailScreen(hero, this)))
                    .bounds(left, rowTop, panelWidth, ROW_HEIGHT - 4)
                    .build();
            this.addRenderableWidget(button);
            index++;
        }
        this.addRenderableWidget(Button.builder(Component.translatable("gui.heroesjourney.close"), b -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 30, 100, 20)
                .build());
    }

    private Component rowLabel(HeroDefinition hero) {
        String statusKey = statusKey(hero);
        return Component.literal("")
                .append(hero.displayName())
                .append(Component.literal("  "))
                .append(Component.translatable(statusKey).withStyle(style -> style.withColor(statusColor(hero))));
    }

    private String statusKey(HeroDefinition hero) {
        HeroData data = ClientHeroDataCache.get();
        if (data.activeHero().equals(hero.id())) {
            return "gui.heroesjourney.status.active";
        }
        HeroProgress progress = data.getProgress(hero.id());
        if (progress != null && hero.questline().isComplete(progress.stageIndex())) {
            return "gui.heroesjourney.status.completed";
        }
        return "gui.heroesjourney.status.available";
    }

    private int statusColor(HeroDefinition hero) {
        HeroData data = ClientHeroDataCache.get();
        if (data.activeHero().equals(hero.id())) {
            return HJTheme.ACCENT;
        }
        HeroProgress progress = data.getProgress(hero.id());
        if (progress != null && hero.questline().isComplete(progress.stageIndex())) {
            return HJTheme.GREEN;
        }
        return HJTheme.TEXT_DIM;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, HJTheme.BACKGROUND);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 16, HJTheme.ACCENT);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
