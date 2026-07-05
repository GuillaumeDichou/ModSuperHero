package com.heroesjourney.client.gui;

import com.heroesjourney.network.HJNetworking;
import com.heroesjourney.network.OpenPuzzlePayload;
import com.heroesjourney.network.PuzzleSolvedPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * Riddle book mini-games. All three types are driven by a single server-generated
 * {@link OpenPuzzlePayload}; validation happens locally on the client (like the rest of this
 * mod's client-driven state, e.g. ability cooldown display) rather than round-tripping every
 * click through the server.
 * <p>
 * The "memory" puzzle shows the target sequence as a static row of swatches instead of flashing
 * it chronologically - a deliberate simplification to avoid a client-side animation/timing state
 * machine; the player still has to reproduce the order correctly.
 */
public class PuzzleScreen extends Screen {

    private static final int[] SEQUENCE_COLORS = {0xFFDD5555, 0xFF55DD55, 0xFF5599DD, 0xFFDDDD55};
    private static final String[] SEQUENCE_NAMES = {
            "gui.heroesjourney.puzzle.color.red", "gui.heroesjourney.puzzle.color.green",
            "gui.heroesjourney.puzzle.color.blue", "gui.heroesjourney.puzzle.color.yellow"
    };

    private final OpenPuzzlePayload payload;
    private List<Integer> sequence = List.of();
    private final List<Integer> playerInput = new ArrayList<>();
    private Component questionText;
    private Component feedback = Component.empty();
    private EditBox answerBox;
    private String cipherAnswer;

    public PuzzleScreen(OpenPuzzlePayload payload) {
        super(Component.translatable("gui.heroesjourney.puzzle.title"));
        this.payload = payload;
    }

    @Override
    protected void init() {
        switch (payload.puzzleType()) {
            case OpenPuzzlePayload.TYPE_MEMORY -> initMemory();
            case OpenPuzzlePayload.TYPE_LOGIC -> initLogic();
            default -> initCipher();
        }
        this.addRenderableWidget(Button.builder(Component.translatable("gui.heroesjourney.close"), b -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 26, 100, 20)
                .build());
    }

    private void initMemory() {
        List<Integer> parsed = new ArrayList<>();
        for (String part : payload.data().split(",")) {
            parsed.add(Integer.parseInt(part));
        }
        sequence = parsed;
        feedback = Component.translatable("gui.heroesjourney.puzzle.memory.instructions");
        int buttonWidth = 70;
        int startX = this.width / 2 - (buttonWidth * SEQUENCE_COLORS.length) / 2;
        for (int i = 0; i < SEQUENCE_COLORS.length; i++) {
            int colorIndex = i;
            this.addRenderableWidget(Button.builder(Component.translatable(SEQUENCE_NAMES[i]), b -> onMemoryClick(colorIndex))
                    .bounds(startX + i * buttonWidth, this.height / 2 + 20, buttonWidth - 4, 20)
                    .build());
        }
    }

    private void onMemoryClick(int colorIndex) {
        playerInput.add(colorIndex);
        int i = playerInput.size() - 1;
        if (!sequence.get(i).equals(colorIndex)) {
            feedback = Component.translatable("gui.heroesjourney.puzzle.failed");
            playerInput.clear();
            return;
        }
        if (playerInput.size() == sequence.size()) {
            onSolved();
        }
    }

    private void initLogic() {
        String[] parts = payload.data().split(OpenPuzzlePayload.SEP, -1);
        String questionKey = parts[0];
        String[] choiceKeys = {parts[1], parts[2], parts[3]};
        int correctIndex = Integer.parseInt(parts[4]);
        questionText = Component.translatable(questionKey);
        int y = this.height / 2 - 10;
        for (int i = 0; i < choiceKeys.length; i++) {
            int choiceIndex = i;
            this.addRenderableWidget(Button.builder(Component.translatable(choiceKeys[i]), b -> {
                if (choiceIndex == correctIndex) {
                    onSolved();
                } else {
                    feedback = Component.translatable("gui.heroesjourney.puzzle.failed");
                }
            }).bounds(this.width / 2 - 130, y + i * 24, 260, 20).build());
        }
    }

    private void initCipher() {
        String[] parts = payload.data().split(OpenPuzzlePayload.SEP, -1);
        String cipherText = parts[0];
        String shift = parts[1];
        cipherAnswer = parts[2];
        questionText = Component.literal(cipherText);
        feedback = Component.translatable("gui.heroesjourney.puzzle.cipher.key", shift);
        answerBox = new EditBox(this.font, this.width / 2 - 100, this.height / 2 + 10, 200, 20,
                Component.translatable("gui.heroesjourney.puzzle.cipher.answer"));
        this.addRenderableWidget(answerBox);
        this.addRenderableWidget(Button.builder(Component.translatable("gui.heroesjourney.puzzle.submit"), b -> onCipherSubmit())
                .bounds(this.width / 2 - 50, this.height / 2 + 36, 100, 20)
                .build());
    }

    private void onCipherSubmit() {
        if (answerBox.getValue().trim().equalsIgnoreCase(cipherAnswer.trim())) {
            onSolved();
        } else {
            feedback = Component.translatable("gui.heroesjourney.puzzle.failed");
        }
    }

    private void onSolved() {
        HJNetworking.sendToServer(new PuzzleSolvedPayload());
        this.minecraft.setScreen(null);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, HJTheme.BACKGROUND);
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(this.font, this.title, this.width / 2, 14, HJTheme.ACCENT);

        if (payload.puzzleType() == OpenPuzzlePayload.TYPE_MEMORY) {
            int swatchSize = 16;
            int totalWidth = sequence.size() * (swatchSize + 4);
            int startX = this.width / 2 - totalWidth / 2;
            int y = this.height / 2 - 30;
            for (int i = 0; i < sequence.size(); i++) {
                int x = startX + i * (swatchSize + 4);
                g.fill(x, y, x + swatchSize, y + swatchSize, SEQUENCE_COLORS[sequence.get(i)]);
                g.renderOutline(x, y, swatchSize, swatchSize, HJTheme.BORDER);
            }
        } else if (questionText != null) {
            int boxWidth = Math.min(320, this.width - 40);
            List<FormattedCharSequence> lines = this.font.split(questionText, boxWidth);
            int ty = this.height / 2 - 60;
            for (FormattedCharSequence line : lines) {
                g.drawString(this.font, line, this.width / 2 - boxWidth / 2, ty, HJTheme.TEXT, false);
                ty += 10;
            }
        }

        g.drawCenteredString(this.font, feedback, this.width / 2, this.height / 2 - 12, HJTheme.TEXT_DIM);
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
