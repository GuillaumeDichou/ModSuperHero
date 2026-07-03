package com.heroesjourney.client.gui;

import com.heroesjourney.dialogue.DialogueChoiceView;
import com.heroesjourney.dialogue.DialogueView;
import com.heroesjourney.network.DialogueChoicePayload;
import com.heroesjourney.network.HJNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.FormattedCharSequence;
import java.util.List;

/** Simple portrait + text + choice-buttons dialogue box, driven entirely by a server-sent {@link DialogueView}. */
public class DialogueScreen extends Screen {

    private DialogueView view;

    public DialogueScreen(DialogueView view) {
        super(view.speakerName());
        this.view = view;
    }

    public void updateView(DialogueView newView) {
        this.view = newView;
        this.clearWidgets();
        buildWidgets();
    }

    @Override
    protected void init() {
        buildWidgets();
    }

    private void buildWidgets() {
        int boxWidth = Math.min(360, this.width - 40);
        int boxBottom = this.height - 20;
        int buttonY = boxBottom - 26;
        if (view.terminal()) {
            this.addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("gui.heroesjourney.close"), b -> this.onClose())
                    .bounds(this.width / 2 - 50, buttonY, 100, 20)
                    .build());
            return;
        }
        List<DialogueChoiceView> choices = view.choices();
        int spacing = 22;
        int startY = buttonY - (choices.size() - 1) * spacing;
        for (int i = 0; i < choices.size(); i++) {
            DialogueChoiceView choice = choices.get(i);
            int y = startY + i * spacing;
            this.addRenderableWidget(Button.builder(choice.label(), b -> {
                HJNetworking.sendToServer(new DialogueChoicePayload(view.npcEntityId(), choice.id()));
            }).bounds(this.width / 2 - boxWidth / 2 + 10, y, boxWidth - 20, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int boxWidth = Math.min(360, this.width - 40);
        int boxLeft = this.width / 2 - boxWidth / 2;
        int boxTop = this.height - 140;
        int boxHeight = 140 - 20;

        guiGraphics.fill(0, 0, this.width, this.height, 0x66000000);
        guiGraphics.fill(boxLeft, boxTop, boxLeft + boxWidth, boxTop + boxHeight, HJTheme.PANEL);
        guiGraphics.renderOutline(boxLeft, boxTop, boxWidth, boxHeight, HJTheme.BORDER);

        guiGraphics.drawString(this.font, view.speakerName(), boxLeft + 10, boxTop + 8, HJTheme.ACCENT, false);

        int textWidth = boxWidth - 20;
        List<FormattedCharSequence> lines = this.font.split(view.text(), textWidth);
        int ty = boxTop + 22;
        for (FormattedCharSequence line : lines) {
            guiGraphics.drawString(this.font, line, boxLeft + 10, ty, HJTheme.TEXT, false);
            ty += 10;
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
