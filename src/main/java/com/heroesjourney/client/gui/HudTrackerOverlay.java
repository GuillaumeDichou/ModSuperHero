package com.heroesjourney.client.gui;

import com.heroesjourney.client.ClientHeroDataCache;
import com.heroesjourney.data.HeroData;
import com.heroesjourney.data.HeroProgress;
import com.heroesjourney.hero.HeroDefinition;
import com.heroesjourney.hero.HeroRegistry;
import com.heroesjourney.quest.QuestObjective;
import com.heroesjourney.quest.QuestStage;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.IGuiLayer;

/** Small "current objective" reminder in the corner of the screen; togglable from the hero detail screen. */
public class HudTrackerOverlay implements IGuiLayer {

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.screen != null) {
            return;
        }
        HeroData data = ClientHeroDataCache.get();
        if (!data.hudTrackerEnabled() || !data.hasActiveHero()) {
            return;
        }
        HeroDefinition hero = HeroRegistry.get(data.activeHero()).orElse(null);
        if (hero == null) {
            return;
        }
        HeroProgress progress = data.getProgress(hero.id());
        if (progress == null) {
            return;
        }
        QuestStage stage = hero.questline().stageAt(progress.stageIndex()).orElse(null);
        if (stage == null) {
            return;
        }
        int x = 6;
        int y = 6;
        guiGraphics.drawString(mc.font, hero.displayName(), x, y, 0xFFC700, true);
        y += 11;
        guiGraphics.drawString(mc.font, stage.title(), x, y, 0xFFFFFF, true);
        for (QuestObjective objective : stage.objectives()) {
            String key = stage.id() + ":" + objective.id();
            int value = progress.progressFor(key);
            boolean done = objective.condition().isComplete(value);
            y += 10;
            Component progressText = objective.condition().describeProgress(value);
            net.minecraft.network.chat.MutableComponent line = Component.literal(done ? "✔ " : "□ ").append(objective.label());
            if (!progressText.getString().isEmpty()) {
                line.append(Component.literal(" (" + progressText.getString() + ")"));
            }
            guiGraphics.drawString(mc.font, line, x, y, done ? 0x55FF55 : 0xCCCCCC, true);
        }
    }
}
