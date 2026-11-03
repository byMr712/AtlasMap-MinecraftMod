package org.webtrade.atlasmapextended;

import net.neoforged.fml.common.Mod;
import org.webtrade.atlasmapextended.client.AtlasMapExtendedCommon;

/** NeoForge entrypoint. The Fabric entrypoint is excluded from the NeoForge JAR. */
@Mod("atlasmapextended")
public class AtlasMapExtendedNeoForge {

    public AtlasMapExtendedNeoForge() {
        AtlasMapExtendedCommon.init();
    }
}
