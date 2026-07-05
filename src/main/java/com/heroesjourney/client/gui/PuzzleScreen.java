package com.heroesjourney.client.gui;

import com.heroesjourney.HeroesJourney;
import com.heroesjourney.config.HJConfig;
import com.heroesjourney.item.puzzle.PuzzleGenerator;
import com.heroesjourney.network.HJNetworking;
import com.heroesjourney.network.OpenPuzzlePayload;
import com.heroesjourney.network.PuzzleSolvedPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Riddle book mini-games. All three types are driven by a single server-generated
 * {@link OpenPuzzlePayload}; validation happens locally on the client (like the rest of this
 * mod's client-driven state, e.g. ability cooldown display) rather than round-tripping every
 * click through the server.
 */
public class PuzzleScreen extends Screen {

    private static final ResourceLocation EVIDENCE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(HeroesJourney.MODID, "textures/gui/puzzle_evidence.png");
    private static final int EVIDENCE_TEXTURE_SIZE = 48;
    private static final int SLIDING_GRID_SIZE = 3;
    private static final int SLIDING_TILE_SCREEN_SIZE = 48;

    private static final int[] MASTERMIND_COLORS = {
            0xFFDD5555, 0xFF55DD55, 0xFF5599DD, 0xFFDDDD55, 0xFFAA55DD, 0xFF55DDDD
    };
    private static final String[] MASTERMIND_COLOR_NAMES = {
            "gui.heroesjourney.puzzle.color.red", "gui.heroesjourney.puzzle.color.green",
            "gui.heroesjourney.puzzle.color.blue", "gui.heroesjourney.puzzle.color.yellow",
            "gui.heroesjourney.puzzle.color.purple", "gui.heroesjourney.puzzle.color.cyan"
    };

    private final OpenPuzzlePayload payload;
    private Component feedback = Component.empty();

    // Mastermind state
    private List<Integer> mastermindSecret = List.of();
    private final List<Integer> mastermindGuess = new ArrayList<>();
    private final List<int[]> mastermindHistory = new ArrayList<>();

    // Sliding puzzle state
    private final List<Integer> slidingArrangement = new ArrayList<>();
    private Integer slidingSelected;
    private int slidingGridLeft;
    private int slidingGridTop;

    // Lockpicking state
    private int lockpickPinIndex;
    private int lockpickPinCount;
    private float lockpickZoneStart;
    private float lockpickZoneWidth;
    private long lockpickStartMillis;
    private final Random lockpickRandom = new Random();

    public PuzzleScreen(OpenPuzzlePayload payload) {
        super(Component.translatable("gui.heroesjourney.puzzle.title"));
        this.payload = payload;
    }

    @Override
    protected void init() {
        switch (payload.puzzleType()) {
            case OpenPuzzlePayload.TYPE_MASTERMIND -> initMastermind();
            case OpenPuzzlePayload.TYPE_SLIDING -> initSliding();
            default -> initLockpick();
        }
        this.addRenderableWidget(Button.builder(Component.translatable("gui.heroesjourney.close"), b -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 26, 100, 20)
                .build());
    }

    // -------------------------------------------------------------------
    // Mastermind ("crack the safe")
    // -------------------------------------------------------------------

    private void initMastermind() {
        List<Integer> parsed = new ArrayList<>();
        for (String part : payload.data().split(",")) {
            parsed.add(Integer.parseInt(part));
        }
        mastermindSecret = parsed;
        feedback = Component.translatable("gui.heroesjourney.puzzle.mastermind.instructions");

        int buttonWidth = 60;
        int colorCount = MASTERMIND_COLORS.length;
        int startX = this.width / 2 - (buttonWidth * colorCount) / 2;
        int colorY = this.height / 2 + 30;
        for (int i = 0; i < colorCount; i++) {
            int colorIndex = i;
            this.addRenderableWidget(Button.builder(Component.translatable(MASTERMIND_COLOR_NAMES[i]), b -> onMastermindPick(colorIndex))
                    .bounds(startX + i * buttonWidth, colorY, buttonWidth - 4, 20)
                    .build());
        }

        this.addRenderableWidget(Button.builder(Component.translatable("gui.heroesjourney.puzzle.mastermind.clear"), b -> mastermindGuess.clear())
                .bounds(this.width / 2 - 105, colorY + 24, 100, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.heroesjourney.puzzle.submit"), b -> onMastermindSubmit())
                .bounds(this.width / 2 + 5, colorY + 24, 100, 20)
                .build());
    }

    private void onMastermindPick(int colorIndex) {
        if (mastermindGuess.size() < mastermindSecret.size()) {
            mastermindGuess.add(colorIndex);
        }
    }

    private void onMastermindSubmit() {
        if (mastermindGuess.size() < mastermindSecret.size()) {
            feedback = Component.translatable("gui.heroesjourney.puzzle.mastermind.incomplete");
            return;
        }
        int colorCount = MASTERMIND_COLORS.length;
        int[] secretRemaining = new int[colorCount];
        int[] guessRemaining = new int[colorCount];
        int exact = 0;
        for (int i = 0; i < mastermindSecret.size(); i++) {
            int s = mastermindSecret.get(i);
            int g = mastermindGuess.get(i);
            if (s == g) {
                exact++;
            } else {
                secretRemaining[s]++;
                guessRemaining[g]++;
            }
        }
        int present = 0;
        for (int c = 0; c < colorCount; c++) {
            present += Math.min(secretRemaining[c], guessRemaining[c]);
        }
        mastermindHistory.add(new int[]{exact, present});
        if (exact == mastermindSecret.size()) {
            onSolved();
            return;
        }
        feedback = Component.translatable("gui.heroesjourney.puzzle.mastermind.feedback", exact, present);
        mastermindGuess.clear();
    }

    // -------------------------------------------------------------------
    // Sliding puzzle ("reassemble the evidence")
    // -------------------------------------------------------------------

    private void initSliding() {
        slidingArrangement.clear();
        for (String part : payload.data().split(",")) {
            slidingArrangement.add(Integer.parseInt(part));
        }
        feedback = Component.translatable("gui.heroesjourney.puzzle.sliding.instructions");
        slidingGridLeft = this.width / 2 - (SLIDING_GRID_SIZE * SLIDING_TILE_SCREEN_SIZE) / 2;
        slidingGridTop = this.height / 2 - (SLIDING_GRID_SIZE * SLIDING_TILE_SCREEN_SIZE) / 2 - 10;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (payload.puzzleType() == OpenPuzzlePayload.TYPE_SLIDING && button == 0) {
            int cell = slidingCellAt(mouseX, mouseY);
            if (cell >= 0) {
                onSlidingClick(cell);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int slidingCellAt(double mouseX, double mouseY) {
        int gridSize = SLIDING_GRID_SIZE * SLIDING_TILE_SCREEN_SIZE;
        if (mouseX < slidingGridLeft || mouseX >= slidingGridLeft + gridSize
                || mouseY < slidingGridTop || mouseY >= slidingGridTop + gridSize) {
            return -1;
        }
        int col = (int) ((mouseX - slidingGridLeft) / SLIDING_TILE_SCREEN_SIZE);
        int row = (int) ((mouseY - slidingGridTop) / SLIDING_TILE_SCREEN_SIZE);
        return row * SLIDING_GRID_SIZE + col;
    }

    private void onSlidingClick(int cell) {
        if (slidingSelected == null) {
            slidingSelected = cell;
            return;
        }
        if (slidingSelected == cell) {
            slidingSelected = null;
            return;
        }
        int a = slidingArrangement.get(slidingSelected);
        int b = slidingArrangement.get(cell);
        slidingArrangement.set(slidingSelected, b);
        slidingArrangement.set(cell, a);
        slidingSelected = null;
        if (isSlidingSolved()) {
            onSolved();
        }
    }

    private boolean isSlidingSolved() {
        for (int i = 0; i < slidingArrangement.size(); i++) {
            if (slidingArrangement.get(i) != i) {
                return false;
            }
        }
        return true;
    }

    // -------------------------------------------------------------------
    // Lock-picking ("pick the gadget's lock")
    // -------------------------------------------------------------------

    private void initLockpick() {
        lockpickPinCount = HJConfig.LOCKPICK_PIN_COUNT.get();
        lockpickPinIndex = 0;
        lockpickStartMillis = System.currentTimeMillis();
        rollLockpickZone();
        feedback = Component.translatable("gui.heroesjourney.puzzle.lockpick.instructions");
        this.addRenderableWidget(Button.builder(Component.translatable("gui.heroesjourney.puzzle.lockpick.pick"), b -> onLockpickAttempt())
                .bounds(this.width / 2 - 60, this.height / 2 + 40, 120, 20)
                .build());
    }

    private void rollLockpickZone() {
        int initial = HJConfig.LOCKPICK_INITIAL_ZONE_WIDTH_PERCENT.get();
        int min = HJConfig.LOCKPICK_MIN_ZONE_WIDTH_PERCENT.get();
        int steps = Math.max(1, lockpickPinCount - 1);
        float t = lockpickPinCount <= 1 ? 1.0F : (float) lockpickPinIndex / steps;
        float widthPercent = initial + (min - initial) * t;
        lockpickZoneWidth = Math.max(0.02F, widthPercent / 100.0F);
        lockpickZoneStart = lockpickRandom.nextFloat() * (1.0F - lockpickZoneWidth);
    }

    private float lockpickCursorPhase() {
        long elapsed = System.currentTimeMillis() - lockpickStartMillis;
        double period = 1400.0;
        double t = (elapsed % (long) period) / period;
        double phase = t < 0.5 ? t * 2 : 2 - t * 2;
        return (float) phase;
    }

    private void onLockpickAttempt() {
        float cursor = lockpickCursorPhase();
        if (cursor >= lockpickZoneStart && cursor <= lockpickZoneStart + lockpickZoneWidth) {
            lockpickPinIndex++;
            if (lockpickPinIndex >= lockpickPinCount) {
                onSolved();
                return;
            }
            rollLockpickZone();
            feedback = Component.translatable("gui.heroesjourney.puzzle.lockpick.pin_solved", lockpickPinIndex, lockpickPinCount);
        } else {
            feedback = Component.translatable("gui.heroesjourney.puzzle.failed");
        }
    }

    // -------------------------------------------------------------------

    private void onSolved() {
        HJNetworking.sendToServer(new PuzzleSolvedPayload());
        this.minecraft.setScreen(null);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, HJTheme.BACKGROUND);
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(this.font, this.title, this.width / 2, 14, HJTheme.ACCENT);

        switch (payload.puzzleType()) {
            case OpenPuzzlePayload.TYPE_MASTERMIND -> renderMastermind(g);
            case OpenPuzzlePayload.TYPE_SLIDING -> renderSliding(g);
            default -> renderLockpick(g);
        }

        g.drawCenteredString(this.font, feedback, this.width / 2, this.height / 2 - 55, HJTheme.TEXT_DIM);
    }

    private void renderMastermind(GuiGraphics g) {
        int slotSize = 20;
        int totalWidth = mastermindSecret.size() * (slotSize + 4);
        int startX = this.width / 2 - totalWidth / 2;
        int y = this.height / 2 - 30;
        for (int i = 0; i < mastermindSecret.size(); i++) {
            int x = startX + i * (slotSize + 4);
            int fill = i < mastermindGuess.size() ? MASTERMIND_COLORS[mastermindGuess.get(i)] : HJTheme.PANEL;
            g.fill(x, y, x + slotSize, y + slotSize, fill);
            g.renderOutline(x, y, slotSize, slotSize, HJTheme.BORDER);
        }

        int historyY = y + slotSize + 10;
        int visibleHistory = Math.min(mastermindHistory.size(), 6);
        for (int h = mastermindHistory.size() - visibleHistory; h < mastermindHistory.size(); h++) {
            int[] result = mastermindHistory.get(h);
            Component line = Component.translatable("gui.heroesjourney.puzzle.mastermind.history", h + 1, result[0], result[1]);
            g.drawCenteredString(this.font, line, this.width / 2, historyY, HJTheme.TEXT);
            historyY += 10;
        }
    }

    private void renderSliding(GuiGraphics g) {
        int uvTileSize = EVIDENCE_TEXTURE_SIZE / SLIDING_GRID_SIZE;
        for (int row = 0; row < SLIDING_GRID_SIZE; row++) {
            for (int col = 0; col < SLIDING_GRID_SIZE; col++) {
                int cell = row * SLIDING_GRID_SIZE + col;
                int tile = slidingArrangement.get(cell);
                int u = (tile % SLIDING_GRID_SIZE) * uvTileSize;
                int v = (tile / SLIDING_GRID_SIZE) * uvTileSize;
                int x = slidingGridLeft + col * SLIDING_TILE_SCREEN_SIZE;
                int y = slidingGridTop + row * SLIDING_TILE_SCREEN_SIZE;
                g.blit(EVIDENCE_TEXTURE, x, y, SLIDING_TILE_SCREEN_SIZE, SLIDING_TILE_SCREEN_SIZE,
                        u, v, uvTileSize, uvTileSize, EVIDENCE_TEXTURE_SIZE, EVIDENCE_TEXTURE_SIZE);
                int borderColor = (slidingSelected != null && slidingSelected == cell) ? HJTheme.ACCENT : HJTheme.BORDER;
                g.renderOutline(x, y, SLIDING_TILE_SCREEN_SIZE, SLIDING_TILE_SCREEN_SIZE, borderColor);
            }
        }
    }

    private void renderLockpick(GuiGraphics g) {
        int barWidth = 240;
        int barHeight = 14;
        int barLeft = this.width / 2 - barWidth / 2;
        int barTop = this.height / 2 - 10;

        g.fill(barLeft, barTop, barLeft + barWidth, barTop + barHeight, HJTheme.PANEL);
        g.renderOutline(barLeft, barTop, barWidth, barHeight, HJTheme.BORDER);

        int zoneX = barLeft + Math.round(lockpickZoneStart * barWidth);
        int zoneWidth = Math.max(2, Math.round(lockpickZoneWidth * barWidth));
        g.fill(zoneX, barTop, zoneX + zoneWidth, barTop + barHeight, HJTheme.GREEN);

        int cursorX = barLeft + Math.round(lockpickCursorPhase() * barWidth);
        g.fill(cursorX - 1, barTop - 4, cursorX + 1, barTop + barHeight + 4, HJTheme.ACCENT);

        Component pinLabel = Component.translatable("gui.heroesjourney.puzzle.lockpick.progress", lockpickPinIndex, lockpickPinCount);
        g.drawCenteredString(this.font, pinLabel, this.width / 2, barTop - 16, HJTheme.TEXT);
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
