package com.heroesjourney.item.puzzle;

/** One logic-riddle entry: translation keys only, so the riddle book is fully localized. */
public record Riddle(String questionKey, String choiceKeyA, String choiceKeyB, String choiceKeyC, int correctIndex) {
}
