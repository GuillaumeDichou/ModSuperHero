package com.heroesjourney.item.puzzle;

/**
 * Plaintext phrases for the "code to crack" puzzle. Generated content (a Caesar-shifted
 * cryptogram) can't be routed through the translation system like the rest of the mod's text -
 * it must exist as real characters server-side to be shifted - so this bank is French-only.
 */
final class CipherBank {

    static final String[] PHRASES = {
            "LE CHEVALIER NOIR",
            "LA PEUR EST UNE ARME",
            "GOTHAM A BESOIN DE MOI",
            "JE SUIS LA VENGEANCE",
            "LA JUSTICE POUR TOUS",
            "LOMBRE PROTEGE LA VILLE",
            "LE SILENCE EST UNE ARME",
            "CONNAIS TON ENNEMI",
            "LA DISCIPLINE FORGE LE HEROS",
            "PATIENCE ET PREPARATION"
    };

    private CipherBank() {
    }
}
