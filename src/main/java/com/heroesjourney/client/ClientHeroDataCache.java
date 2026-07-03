package com.heroesjourney.client;

import com.heroesjourney.data.HeroData;

/**
 * Client-side mirror of the local player's {@link HeroData}, refreshed every time the server
 * sends a {@code SyncHeroDataPayload}. The roster/detail screens and the HUD tracker all read
 * from this rather than trying to access the (server-only) attachment directly.
 */
public final class ClientHeroDataCache {

    private static HeroData data = new HeroData();

    private ClientHeroDataCache() {
    }

    public static void set(HeroData newData) {
        data = newData;
    }

    public static HeroData get() {
        return data;
    }
}
