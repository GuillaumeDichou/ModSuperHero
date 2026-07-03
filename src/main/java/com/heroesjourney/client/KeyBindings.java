package com.heroesjourney.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public final class KeyBindings {

    public static final String CATEGORY = "key.categories.heroesjourney";

    public static final KeyMapping OPEN_ROSTER = new KeyMapping(
            "key.heroesjourney.open_roster", InputConstants.Type.KEYSYM, InputConstants.KEY_K, CATEGORY);

    public static final KeyMapping USE_ABILITY = new KeyMapping(
            "key.heroesjourney.use_ability", InputConstants.Type.KEYSYM, InputConstants.KEY_V, CATEGORY);

    private KeyBindings() {
    }
}
