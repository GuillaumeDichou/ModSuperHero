package com.heroesjourney.client.gui;

import com.heroesjourney.client.ClientHeroDataCache;
import com.heroesjourney.data.HeroData;
import com.heroesjourney.data.HeroProgress;
import com.heroesjourney.hero.HeroDefinition;
import com.heroesjourney.network.ActivateHeroPayload;
import com.heroesjourney.network.HJNetworking;
import com.heroesjourney.network.SetHudTrackerPayload;
import com.heroesjourney.quest.QuestObjective;
import com.heroesjourney.quest.QuestStage;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

/**
 * Screen 2: a hero's questline, consultable even when that hero isn't active. Each stage shows
 * done / in-progress-with-counter / locked; there's an "Activate" button (with confirmation) and
 * an optional-HUD-tracker checkbox. The current stage additionally shows its narrative flavor
 * text (see {@link QuestStage#description()}) above its objectives.
 */
public class HeroDetailScreen extends Screen {

    private final HeroDefinition hero;
    private final Screen parent;
    private int scroll;

    public HeroDetailScreen(HeroDefinition hero, Screen parent) {
        super(hero.displayName());
        this.hero = hero;
        this.parent = parent;
    }

    @Override
    protected void init() {
        HeroData data = ClientHeroDataCache.get();
        boolean active = data.activeHero().equals(hero.id());

        Component activateLabel = active
                ? Component.translatable("gui.heroesjourney.active_hero")
                : Component.translatable("gui.heroesjourney.activate_hero");
        Button activateButton = Button.builder(activateLabel, b -> onActivateClicked())
                .bounds(this.width / 2 - 100, this.height - 54, 200, 20)
                .build();
        activateButton.active = !active;
        this.addRenderableWidget(activateButton);

        boolean hudEnabled = data.hudTrackerEnabled();
        this.addRenderableWidget(Button.builder(hudTrackerLabel(hudEnabled), b -> {
            boolean newValue = !ClientHeroDataCache.get().hudTrackerEnabled();
            ClientHeroDataCache.get().setHudTrackerEnabled(newValue);
            HJNetworking.sendToServer(new SetHudTrackerPayload(newValue));
            b.setMessage(hudTrackerLabel(newValue));
        }).bounds(this.width / 2 - 100, this.height - 30, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.heroesjourney.back"), b -> this.minecraft.setScreen(parent))
                .bounds(8, this.height - 30, 80, 20)
                .build());
    }

    private Component hudTrackerLabel(boolean enabled) {
        return Component.translatable("gui.heroesjourney.hud_tracker", Component.translatable(enabled ? "gui.heroesjourney.on" : "gui.heroesjourney.off"));
    }

    private void onActivateClicked() {
        this.minecraft.setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        HJNetworking.sendToServer(new ActivateHeroPayload(hero.id()));
                    }
                    this.minecraft.setScreen(this);
                },
                Component.translatable("gui.heroesjourney.confirm_activate.title"),
                Component.translatable("gui.heroesjourney.confirm_activate.message", hero.displayName())
        ));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, HJTheme.BACKGROUND);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 14, HJTheme.ACCENT);

        HeroData data = ClientHeroDataCache.get();
        HeroProgress progress = data.getProgress(hero.id());
        int currentStage = progress == null ? 0 : progress.stageIndex();

        List<QuestStage> stages = hero.questline().stages();
        int y = 34 - scroll;
        int left = Math.max(20, this.width / 2 - 160);
        int width = Math.min(320, this.width - 40);
        for (int i = 0; i < stages.size(); i++) {
            QuestStage stage = stages.get(i);
            if (y > this.height - 60 || y < 20) {
                y += stageHeight(stage, i, currentStage, width);
                continue;
            }
            y = renderStage(guiGraphics, stage, i, currentStage, progress, left, y, width);
        }
    }

    private int stageHeight(QuestStage stage, int index, int currentStage, int width) {
        if (index > currentStage) {
            return 14;
        }
        int height = 14 + stage.objectives().size() * 10;
        if (index == currentStage) {
            height += descriptionLines(stage, width).size() * 10 + 3;
        }
        return height;
    }

    private List<FormattedCharSequence> descriptionLines(QuestStage stage, int width) {
        return this.font.split(stage.description(), width);
    }

    private int renderStage(GuiGraphics g, QuestStage stage, int index, int currentStage, HeroProgress progress, int left, int y, int width) {
        String status;
        int color;
        if (index < currentStage) {
            status = "✔";
            color = HJTheme.GREEN;
        } else if (index == currentStage) {
            status = "▶";
            color = HJTheme.ACCENT;
        } else {
            status = "🔒";
            color = HJTheme.TEXT_DIM;
        }
        MutableComponent line = Component.literal(status + " ").append(stage.title());
        g.drawString(this.font, line, left, y, index > currentStage ? HJTheme.TEXT_DIM : color, false);
        y += 11;
        if (index == currentStage && progress != null) {
            for (FormattedCharSequence descLine : descriptionLines(stage, width)) {
                g.drawString(this.font, descLine, left, y, HJTheme.TEXT_DIM, false);
                y += 10;
            }
            y += 3;
            for (QuestObjective objective : stage.objectives()) {
                String key = stage.id() + ":" + objective.id();
                int value = progress.progressFor(key);
                boolean done = objective.condition().isComplete(value);
                Component progressText = objective.condition().describeProgress(value);
                MutableComponent objLine = Component.literal(done ? "  ✓ " : "  - ").append(objective.label());
                if (!progressText.getString().isEmpty()) {
                    objLine.append(Component.literal(" (" + progressText.getString() + ")"));
                }
                g.drawString(this.font, objLine, left, y, done ? HJTheme.GREEN : HJTheme.TEXT, false);
                y += 10;
            }
        } else if (index < currentStage) {
            y += 1;
        }
        return y + 3;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.max(0, scroll - (int) (scrollY * 12));
        return true;
    }
}
