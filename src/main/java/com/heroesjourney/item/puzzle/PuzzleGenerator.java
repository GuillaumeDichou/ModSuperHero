package com.heroesjourney.item.puzzle;

import com.heroesjourney.config.HJConfig;
import com.heroesjourney.network.OpenPuzzlePayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;

/** Picks one of the three puzzle types at random and generates its server-side content. */
public final class PuzzleGenerator {

    /** Fixed 3x3 layout to match the sliced evidence-photo texture; not worth exposing in config. */
    public static final int SLIDING_TILE_COUNT = 9;

    private PuzzleGenerator() {
    }

    public static OpenPuzzlePayload generate(RandomSource random) {
        return switch (random.nextInt(3)) {
            case 0 -> generateMastermind(random);
            case 1 -> generateSliding(random);
            default -> generateLockpick();
        };
    }

    private static OpenPuzzlePayload generateMastermind(RandomSource random) {
        int length = HJConfig.MASTERMIND_CODE_LENGTH.get();
        int symbols = HJConfig.MASTERMIND_SYMBOL_COUNT.get();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(random.nextInt(symbols));
        }
        return new OpenPuzzlePayload(OpenPuzzlePayload.TYPE_MASTERMIND, sb.toString());
    }

    private static OpenPuzzlePayload generateSliding(RandomSource random) {
        List<Integer> order;
        do {
            order = shuffledIndices(random, SLIDING_TILE_COUNT);
        } while (isIdentity(order));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < order.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(order.get(i));
        }
        return new OpenPuzzlePayload(OpenPuzzlePayload.TYPE_SLIDING, sb.toString());
    }

    private static OpenPuzzlePayload generateLockpick() {
        // Nothing to hide - the target zones are a reflex challenge the player can see, not a
        // secret to guess - so the client generates its own zones locally; no data needed here.
        return new OpenPuzzlePayload(OpenPuzzlePayload.TYPE_LOCKPICK, "");
    }

    private static List<Integer> shuffledIndices(RandomSource random, int count) {
        List<Integer> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(i);
        }
        for (int i = list.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            list.set(i, list.set(j, list.get(i)));
        }
        return list;
    }

    private static boolean isIdentity(List<Integer> order) {
        for (int i = 0; i < order.size(); i++) {
            if (order.get(i) != i) {
                return false;
            }
        }
        return true;
    }
}
