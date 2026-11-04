package org.webtrade.atlasmapextended.mixin.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.webtrade.atlasmapextended.client.AtlasMapExtendedCommon;
import org.webtrade.atlasmapextended.client.map.MapManager;

/** Replaces Fabric API's client tick and client stopping events. */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void atlasmapextended$tick(CallbackInfo ci) {
        AtlasMapExtendedCommon.onTick((Minecraft) (Object) this);
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void atlasmapextended$close(CallbackInfo ci) {
        MapManager.onDisconnect();
    }
}
