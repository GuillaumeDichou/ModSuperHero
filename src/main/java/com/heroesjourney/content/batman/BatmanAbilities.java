package com.heroesjourney.content.batman;

/** Ability/flag id constants shared between {@link BatmanContent}, the client keybind and effect gating. */
public final class BatmanAbilities {

    public static final String HERO_ID = "batman";

    /** Chest-glow ability (quest 4 reward) - see {@link BatmanAbilityEffects#chestGlow}. */
    public static final String CHEST_GLOW = "chest_glow";

    public static final String FLAG_STEALTH_TRAINED = "stealth_trained";
    public static final String FLAG_RUN_TRAINED = "run_trained";
    public static final String FLAG_JUMP_TRAINED = "jump_trained";
    public static final String FLAG_SWIM_TRAINED = "swim_trained";
    public static final String FLAG_MARTIAL_ARTS_TRAINED = "martial_arts_trained";
    public static final String FLAG_ARMOR_RECIPES_UNLOCKED = "armor_recipes_unlocked";
    public static final String FLAG_GADGET_RECIPES_UNLOCKED = "gadget_recipes_unlocked";

    private BatmanAbilities() {
    }
}
