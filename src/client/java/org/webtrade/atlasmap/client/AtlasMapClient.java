package org.webtrade.atlasmap.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * Fabric entrypoint. The NeoForge JAR excludes this class and uses
 * {@code org.webtrade.atlasmap.AtlasMapNeoForge} instead.
 */
public class AtlasMapClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AtlasMapCommon.init();
    }
}
