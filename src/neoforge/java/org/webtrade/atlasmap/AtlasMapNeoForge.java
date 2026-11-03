package org.webtrade.atlasmap;

import net.neoforged.fml.common.Mod;
import org.webtrade.atlasmap.client.AtlasMapCommon;

/** NeoForge entrypoint. The Fabric entrypoint is excluded from the NeoForge JAR. */
@Mod("atlasmap")
public class AtlasMapNeoForge {

    public AtlasMapNeoForge() {
        AtlasMapCommon.init();
    }
}
