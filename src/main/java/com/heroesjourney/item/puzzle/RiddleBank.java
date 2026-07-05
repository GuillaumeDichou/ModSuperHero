package com.heroesjourney.item.puzzle;

import java.util.List;

/** Fixed pool of deduction riddles for the "logic" puzzle type; text lives entirely in the lang files. */
public final class RiddleBank {

    public static final List<Riddle> RIDDLES = List.of(
            riddle(1, 0),
            riddle(2, 1),
            riddle(3, 2),
            riddle(4, 0),
            riddle(5, 1),
            riddle(6, 2),
            riddle(7, 0),
            riddle(8, 1),
            riddle(9, 2),
            riddle(10, 0),
            riddle(11, 1),
            riddle(12, 2),
            riddle(13, 0),
            riddle(14, 1),
            riddle(15, 2),
            riddle(16, 0)
    );

    private static Riddle riddle(int index, int correctIndex) {
        String base = "puzzle.heroesjourney.riddle." + index;
        return new Riddle(base + ".question", base + ".a", base + ".b", base + ".c", correctIndex);
    }

    private RiddleBank() {
    }
}
