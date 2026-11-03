package org.webtrade.atlasmapextended.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * Fabric entrypoint. The NeoForge JAR excludes this class and uses
 * {@code org.webtrade.atlasmapextended.AtlasMapExtendedNeoForge} instead.
 */
public class AtlasMapExtendedClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AtlasMapExtendedCommon.init();
    }
}
