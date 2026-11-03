package org.webtrade.atlasmap.mixin.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.webtrade.atlasmap.client.AtlasMapCommon;
import org.webtrade.atlasmap.client.map.MapManager;

/** Replaces Fabric API's client tick and client stopping events. */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void atlasmap$tick(CallbackInfo ci) {
        AtlasMapCommon.onTick((Minecraft) (Object) this);
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void atlasmap$close(CallbackInfo ci) {
        MapManager.onDisconnect();
    }
}
