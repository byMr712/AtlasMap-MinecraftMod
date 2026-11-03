package org.webtrade.mapminecraft;

import net.neoforged.fml.common.Mod;
import org.webtrade.mapminecraft.client.MapminecraftCommon;

/** NeoForge entrypoint. The Fabric entrypoint is excluded from the NeoForge JAR. */
@Mod("mapminecraft")
public class MapminecraftNeoForge {

    public MapminecraftNeoForge() {
        MapminecraftCommon.init();
    }
}
