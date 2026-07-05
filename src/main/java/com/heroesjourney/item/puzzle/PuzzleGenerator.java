package com.heroesjourney.item.puzzle;

import com.heroesjourney.config.HJConfig;
import com.heroesjourney.network.OpenPuzzlePayload;
import net.minecraft.util.RandomSource;

/** Picks one of the three puzzle types at random and generates its server-side content. */
public final class PuzzleGenerator {

    private PuzzleGenerator() {
    }

    public static OpenPuzzlePayload generate(RandomSource random) {
        return switch (random.nextInt(3)) {
            case 0 -> generateMemory(random);
            case 1 -> generateLogic(random);
            default -> generateCipher(random);
        };
    }

    private static OpenPuzzlePayload generateMemory(RandomSource random) {
        int length = HJConfig.PUZZLE_MEMORY_LENGTH.get();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(random.nextInt(4));
        }
        return new OpenPuzzlePayload(OpenPuzzlePayload.TYPE_MEMORY, sb.toString());
    }

    private static OpenPuzzlePayload generateLogic(RandomSource random) {
        Riddle riddle = RiddleBank.RIDDLES.get(random.nextInt(RiddleBank.RIDDLES.size()));
        String data = String.join(OpenPuzzlePayload.SEP,
                riddle.questionKey(), riddle.choiceKeyA(), riddle.choiceKeyB(), riddle.choiceKeyC(),
                String.valueOf(riddle.correctIndex()));
        return new OpenPuzzlePayload(OpenPuzzlePayload.TYPE_LOGIC, data);
    }

    private static OpenPuzzlePayload generateCipher(RandomSource random) {
        String plain = CipherBank.PHRASES[random.nextInt(CipherBank.PHRASES.length)];
        int shift = 1 + random.nextInt(25);
        String cipherText = caesarShift(plain, shift);
        String data = String.join(OpenPuzzlePayload.SEP, cipherText, String.valueOf(shift), plain);
        return new OpenPuzzlePayload(OpenPuzzlePayload.TYPE_CIPHER, data);
    }

    private static String caesarShift(String text, int shift) {
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                sb.append((char) ((c - 'A' + shift) % 26 + 'A'));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
