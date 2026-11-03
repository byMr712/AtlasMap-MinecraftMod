package org.webtrade.mapminecraft.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * Fabric entrypoint. The NeoForge JAR excludes this class and uses
 * {@code org.webtrade.mapminecraft.MapminecraftNeoForge} instead.
 */
public class MapminecraftClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MapminecraftCommon.init();
    }
}
